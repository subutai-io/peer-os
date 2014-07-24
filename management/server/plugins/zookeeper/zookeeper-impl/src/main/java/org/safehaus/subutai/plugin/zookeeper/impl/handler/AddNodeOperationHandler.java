package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ConfigParams;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperSetupStrategy;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;


/**
 * Adds ne node to ZK cluster. Install over a newly created lxc or over an existing hadoop cluster node
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private String lxcHostname;


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            addStandalone( config );
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            addOverHadoop( config );
        }
    }


    private void addOverHadoop( final ZookeeperClusterConfig config ) {

        po.addLog( "Installing over a hadoop cluster node..." );

        //check if node agent is connected
        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( lxcAgent == null ) {
            po.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( lxcAgent ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Util.wrapAgentToSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }


        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        if ( result.getStdOut().contains( "ksks-zookeeper" ) ) {
            po.addLogFailed(
                    String.format( "Node %s already has Zookeeper installed\nInstallation aborted", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
            po.addLogFailed( String.format( "Node %s has no Hadoop installation\nInstallation aborted", lxcHostname ) );
            return;
        }


        config.getNodes().add( lxcAgent );

        po.addLog( String.format( "Installing %s...", ZookeeperClusterConfig.PRODUCT_KEY ) );

        //install
        Command installCommand = Commands.getInstallCommand( Util.wrapAgentToSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasCompleted() ) {
            po.addLog( "Installation succeeded\nUpdating db..." );
            //update db
            if ( manager.getDbManager().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, clusterName, config ) ) {
                po.addLog( "Cluster info updated in DB\nReconfiguring cluster..." );

                //reconfigure cluster
                Command configureClusterCommand;
                try {
                    configureClusterCommand = Commands.getConfigureClusterCommand( config.getNodes(),
                            ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                            ZookeeperSetupStrategy.prepareConfiguration( config.getNodes() ),
                            ConfigParams.CONFIG_FILE_PATH.getParamValue() );
                }
                catch ( ClusterConfigurationException e ) {
                    po.addLogFailed( String.format( "Error reconfiguring cluster %s", e.getMessage() ) );
                    return;
                }

                manager.getCommandRunner().runCommand( configureClusterCommand );

                if ( configureClusterCommand.hasSucceeded() ) {
                    po.addLog( "Cluster reconfigured\nRestarting cluster..." );
                    //restart all nodes
                    Command restartCommand = Commands.getRestartCommand( config.getNodes() );
                    final AtomicInteger count = new AtomicInteger();
                    manager.getCommandRunner().runCommand( restartCommand, new CommandCallback() {
                        @Override
                        public void onResponse( Response response, AgentResult agentResult, Command command ) {
                            if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                                if ( count.incrementAndGet() == config.getNodes().size() ) {
                                    stop();
                                }
                            }
                        }
                    } );
                    if ( count.get() == config.getNodes().size() ) {
                        po.addLogDone( "Cluster restarted successfully\nDone" );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Failed to restart cluster, %s", restartCommand.getAllErrors() ) );
                    }
                }
                else {
                    po.addLogFailed( String.format(
                            "Cluster reconfiguration failed, %s.\nPlease reconfigure cluster manually and restart it",
                            configureClusterCommand.getAllErrors() ) );
                }
            }
            else {
                po.addLogFailed( "Error while updating cluster info in DB. Check logs\nFailed" );
            }
        }
        else {
            po.addLogFailed( String.format( "Installation failed, %s\nUse Terminal Module to cleanup",
                    installCommand.getAllErrors() ) );
        }
    }


    private void addStandalone( final ZookeeperClusterConfig config ) {
        try {

            //create lxc
            po.addLog( "Creating lxc container..." );

            Set<Agent> agents = manager.getContainerManager().clone( ZookeeperSetupStrategy.TEMPLATE_NAME, 1, null,
                    ZookeeperSetupStrategy.getNodePlacementStrategy() );

            Agent agent = agents.iterator().next();

            config.getNodes().add( agent );

            po.addLog( String.format( "Lxc container created successfully\nUpdating db...",
                    ZookeeperClusterConfig.PRODUCT_KEY ) );

            //update db
            if ( manager.getDbManager().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, clusterName, config ) ) {
                po.addLog( "Cluster info updated in DB\nReconfiguring cluster..." );

                //reconfigure cluster
                Command configureClusterCommand;
                try {
                    configureClusterCommand = Commands.getConfigureClusterCommand( config.getNodes(),
                            ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                            ZookeeperSetupStrategy.prepareConfiguration( config.getNodes() ),
                            ConfigParams.CONFIG_FILE_PATH.getParamValue() );
                }
                catch ( ClusterConfigurationException e ) {
                    po.addLogFailed( String.format( "Error reconfiguring cluster %s", e.getMessage() ) );
                    return;
                }

                manager.getCommandRunner().runCommand( configureClusterCommand );

                if ( configureClusterCommand.hasSucceeded() ) {
                    po.addLog( "Cluster reconfigured\nRestarting cluster..." );
                    //restart all nodes
                    Command restartCommand = Commands.getRestartCommand( config.getNodes() );
                    final AtomicInteger count = new AtomicInteger();
                    manager.getCommandRunner().runCommand( restartCommand, new CommandCallback() {
                        @Override
                        public void onResponse( Response response, AgentResult agentResult, Command command ) {
                            if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                                if ( count.incrementAndGet() == config.getNodes().size() ) {
                                    stop();
                                }
                            }
                        }
                    } );
                    if ( count.get() == config.getNodes().size() ) {
                        po.addLogDone( "Cluster restarted successfully\nDone" );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Failed to restart cluster, %s", restartCommand.getAllErrors() ) );
                    }
                }
                else {
                    po.addLogFailed( String.format(
                            "Cluster reconfiguration failed, %s.\nPlease reconfigure cluster manually and restart it",
                            configureClusterCommand.getAllErrors() ) );
                }
            }
            else {
                po.addLogFailed(
                        "Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed" );
            }
        }
        catch ( LxcCreateException ex ) {
            po.addLogFailed( ex.getMessage() );
        }
    }
}
