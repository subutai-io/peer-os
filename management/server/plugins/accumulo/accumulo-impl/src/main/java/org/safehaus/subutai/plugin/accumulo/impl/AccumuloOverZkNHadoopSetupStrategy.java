package org.safehaus.subutai.plugin.accumulo.impl;


import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
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
 * This is an accumulo cluster setup strategy over existing Hadoop & ZK clusters
 */
public class AccumuloOverZkNHadoopSetupStrategy implements ClusterSetupStrategy {


    private final AccumuloImpl accumuloManager;
    private final ProductOperation po;
    private final AccumuloClusterConfig accumuloClusterConfig;


    public AccumuloOverZkNHadoopSetupStrategy( final AccumuloClusterConfig accumuloClusterConfig,
                                               final ProductOperation po, final AccumuloImpl accumuloManager ) {

        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( accumuloManager, "Accumulo manager is null" );

        this.po = po;
        this.accumuloClusterConfig = accumuloClusterConfig;
        this.accumuloManager = accumuloManager;
    }


    @Override
    public AccumuloClusterConfig setup() throws ClusterSetupException {
        if ( accumuloClusterConfig.getMasterNode() == null || accumuloClusterConfig.getGcNode() == null
                || accumuloClusterConfig.getMonitor() == null || Strings
                .isNullOrEmpty( accumuloClusterConfig.getClusterName() ) || CollectionUtil
                .isCollectionEmpty( accumuloClusterConfig.getTracers() ) || CollectionUtil
                .isCollectionEmpty( accumuloClusterConfig.getSlaves() ) || Strings
                .isNullOrEmpty( accumuloClusterConfig.getInstanceName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getPassword() ) ) {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( accumuloManager.getCluster( accumuloClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", accumuloClusterConfig.getClusterName() ) );
        }

        HadoopClusterConfig hadoopClusterConfig =
                accumuloManager.getHadoopManager().getCluster( accumuloClusterConfig.getHadoopClusterName() );

        if ( hadoopClusterConfig == null ) {
            throw new ClusterSetupException( String.format( "Hadoop cluster with name '%s' not found",
                    accumuloClusterConfig.getHadoopClusterName() ) );
        }

        ZookeeperClusterConfig zookeeperClusterConfig =
                accumuloManager.getZkManager().getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        if ( zookeeperClusterConfig == null ) {
            throw new ClusterSetupException( String.format( "ZK cluster with name '%s' not found",
                    accumuloClusterConfig.getZookeeperClusterName() ) );
        }


        if ( !hadoopClusterConfig.getAllNodes().containsAll( accumuloClusterConfig.getAllNodes() ) ) {
            throw new ClusterSetupException( String.format( "Not all supplied nodes belong to Hadoop cluster %s",
                    hadoopClusterConfig.getClusterName() ) );
        }

        po.addLog( "Checking prerequisites..." );

        //check installed subutai packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( accumuloClusterConfig.getAllNodes() );
        accumuloManager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            throw new ClusterSetupException( "Failed to check presence of installed subutai packages" );
        }

        for ( Agent node : accumuloClusterConfig.getAllNodes() ) {
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_NAME ) ) {
                throw new ClusterSetupException(
                        String.format( "Node %s already has Accumulo installed", node.getHostname() ) );
            }
            else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
                throw new ClusterSetupException(
                        String.format( "Node %s has no Hadoop installation", node.getHostname() ) );
            }
        }


        po.addLog( "Installing Accumulo..." );

        //install
        Command installCommand = Commands.getInstallCommand( accumuloClusterConfig.getAllNodes() );
        accumuloManager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() ) {
            po.addLog( "Installation succeeded" );

            try {
                new ClusterConfiguration( po, accumuloManager )
                        .configureCluster( accumuloClusterConfig, zookeeperClusterConfig );
            }
            catch ( ClusterConfigurationException e ) {
                throw new ClusterSetupException( e.getMessage() );
            }
        }
        else {
            throw new ClusterSetupException(
                    String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }


        return accumuloClusterConfig;
    }
}
