package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.settings.Common;

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
        if ( zookeeperClusterConfig == null ||
                Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( zookeeperClusterConfig.getTemplateName() ) ||
                zookeeperClusterConfig.getNumberOfNodes() <= 0 ) {
            po.addLogFailed( "Malformed configuration\nZookeeper installation aborted" );
        }

        if ( zookeeperManager.getCluster( zookeeperClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            zookeeperClusterConfig.getClusterName() ) );
        }

        po.addLog( "Installing over hadoop cluster nodes" );

        //check if node agent is connected
        for ( Iterator<Agent> it = zookeeperClusterConfig.getNodes().iterator(); it.hasNext(); ) {
            Agent node = it.next();
            if ( zookeeperManager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLog( String.format( "Node %s is not connected. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        //no nodes connected
        if ( zookeeperClusterConfig.getNodes().isEmpty() ) {
            throw new ClusterSetupException( "No nodes eligible for installation" );
        }

        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( zookeeperClusterConfig.getNodes() );
        zookeeperManager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            throw new ClusterSetupException( "Failed to check presence of installed subutai packages" );
        }

        //omit nodes without Hadoop installed
        for ( Iterator<Agent> it = zookeeperClusterConfig.getNodes().iterator(); it.hasNext(); ) {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) ) {
                po.addLog(
                        String.format( "Node %s already has Zookeeper installed. Omitting this node from installation",
                                node.getHostname() ) );
                it.remove();
            }
            else if ( !result.getStdOut()
                             .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        //no nodes with Hadoop
        if ( zookeeperClusterConfig.getNodes().isEmpty() ) {
            throw new ClusterSetupException( "No nodes eligible for installation. Operation aborted" );
        }

        po.addLog( String.format( "Installing Zookeeper on %s...", zookeeperClusterConfig.getNodes() ) );

        //install
        Command installCommand = Commands.getInstallCommand( zookeeperClusterConfig.getNodes() );
        zookeeperManager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() ) {
            po.addLog( "Installation succeeded\nConfiguring cluster..." );

            Command configureClusterCommand;
            try {
                configureClusterCommand = Commands.getConfigureClusterCommand( zookeeperClusterConfig.getNodes(),
                        ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                        ZookeeperStandaloneSetupStrategy.prepareConfiguration( zookeeperClusterConfig.getNodes() ),
                        ConfigParams.CONFIG_FILE_PATH.getParamValue() );
            }
            catch ( ClusterConfigurationException e ) {
                throw new ClusterSetupException( String.format( "Error configuring cluster %s", e.getMessage() ) );
            }

            //configure ZK cluster
            zookeeperManager.getCommandRunner().runCommand( configureClusterCommand );

            if ( configureClusterCommand.hasSucceeded() ) {

                po.addLog( String.format( "Cluster configured\nStarting %s...", ZookeeperClusterConfig.PRODUCT_KEY ) );
                //start all nodes
                Command startCommand = Commands.getStartCommand( zookeeperClusterConfig.getNodes() );
                final AtomicInteger count = new AtomicInteger();
                zookeeperManager.getCommandRunner().runCommand( startCommand, new CommandCallback() {
                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command ) {
                        if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                            if ( count.incrementAndGet() == zookeeperClusterConfig.getNodes().size() ) {
                                stop();
                            }
                        }
                    }
                } );

                if ( count.get() == zookeeperClusterConfig.getNodes().size() ) {
                    po.addLog( String.format( "Starting %s succeeded\nDone", ZookeeperClusterConfig.PRODUCT_KEY ) );
                }
                else {
                    po.addLog( String.format( "Starting %s failed, %s, skipping...", ZookeeperClusterConfig.PRODUCT_KEY,
                            startCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterSetupException( String.format(
                        "Failed to configure cluster, %s\nPlease configure cluster manually and restart it",
                        configureClusterCommand.getAllErrors() ) );
            }
        }
        else {
            throw new ClusterSetupException(
                    String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }

        return zookeeperClusterConfig;
    }
}
