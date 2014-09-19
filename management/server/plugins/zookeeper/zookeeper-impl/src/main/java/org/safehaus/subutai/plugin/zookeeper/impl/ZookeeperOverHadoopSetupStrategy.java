package org.safehaus.subutai.plugin.zookeeper.impl;


import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * ZK cluster setup strategy over an existing Hadoop cluster
 */
public class ZookeeperOverHadoopSetupStrategy implements ClusterSetupStrategy {

    private final ZookeeperClusterConfig zookeeperClusterConfig;
    private final ProductOperation po;
    private final ZookeeperImpl zookeeperManager;


    public ZookeeperOverHadoopSetupStrategy( final ZookeeperClusterConfig zookeeperClusterConfig,
                                             final ProductOperation po, final ZookeeperImpl zookeeperManager ) {
        Preconditions.checkNotNull( zookeeperClusterConfig, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( zookeeperManager, "ZK manager is null" );

        this.zookeeperClusterConfig = zookeeperClusterConfig;
        this.po = po;
        this.zookeeperManager = zookeeperManager;
    }


    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) ||
                zookeeperClusterConfig.getNodes() == null || zookeeperClusterConfig.getNodes().isEmpty() ) {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( zookeeperManager.getCluster( zookeeperClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", zookeeperClusterConfig.getClusterName() ) );
        }

        //check if node agent is connected
        for ( Agent node : zookeeperClusterConfig.getNodes() ) {
            if ( zookeeperManager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                throw new ClusterSetupException( String.format( "Node %s is not connected", node.getHostname() ) );
            }
        }

        HadoopClusterConfig hadoopClusterConfig =
                zookeeperManager.getHadoopManager().getCluster( zookeeperClusterConfig.getHadoopClusterName() );
        if ( hadoopClusterConfig == null ) {
            throw new ClusterSetupException(
                    String.format( "Hadoop cluster %s not found", zookeeperClusterConfig.getHadoopClusterName() ) );
        }

        if ( !hadoopClusterConfig.getAllNodes().containsAll( zookeeperClusterConfig.getNodes() ) ) {
            throw new ClusterSetupException( String.format( "Not all specified ZK nodes belong to %s Hadoop cluster",
                    hadoopClusterConfig.getClusterName() ) );
        }

        po.addLog( "Checking prerequisites..." );

        //check installed subutai packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( zookeeperClusterConfig.getNodes() );
        zookeeperManager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            throw new ClusterSetupException( "Failed to check presence of installed subutai packages" );
        }

        for ( Agent node : zookeeperClusterConfig.getNodes() ) {
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) ) {
                throw new ClusterSetupException(
                        String.format( "Node %s already has Zookeeper installed", node.getHostname() ) );
            }
            else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
                throw new ClusterSetupException(
                        String.format( "Node %s has no Hadoop installed", node.getHostname() ) );
            }
        }

        po.addLog( String.format( "Installing Zookeeper on %s...", zookeeperClusterConfig.getNodes() ) );

        //install
        Command installCommand = Commands.getInstallCommand( zookeeperClusterConfig.getNodes() );
        zookeeperManager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() ) {
            po.addLog( "Installation succeeded\nConfiguring cluster..." );


            try {
                new ClusterConfiguration( zookeeperManager, po ).configureCluster( zookeeperClusterConfig );
            }
            catch ( ClusterConfigurationException e ) {
                throw new ClusterSetupException( e.getMessage() );
            }

            po.addLog( "Saving cluster information to database..." );

            zookeeperManager.getPluginDAO()
                            .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, zookeeperClusterConfig.getClusterName(),
                                    zookeeperClusterConfig );
            po.addLog( "Cluster information saved to database" );
        }
        else {
            throw new ClusterSetupException(
                    String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }

        return zookeeperClusterConfig;
    }
}
