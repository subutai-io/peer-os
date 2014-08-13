package org.safehaus.subutai.impl.zookeeper.handler;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;


/**
 * Adds ne node to ZK cluster. Install over a newly created lxc or over an existing hadoop cluster node
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private String lxcHostname;


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Adding node to %s", clusterName ) );
    }


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        final Config config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( config.isStandalone() ) {
            addStandalone( config );
        }
        else {
            if ( Strings.isNullOrEmpty( lxcHostname ) ) {
                po.addLogFailed( "Lxc hostname to install ZK is not specified" );
                return;
            }
            addOverHadoop( config );
        }
    }


    private void addOverHadoop( final Config config ) {

        po.addLog( "Installing over a hadoop cluster node..." );

        //check if node agent is connected
        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( lxcAgent == null ) {
            po.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( lxcAgent ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        po.addLog( "Checking prerequisites..." );

        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Util.wrapAgentToSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed subutai packages" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        if ( result.getStdOut().contains( "ksks-zookeeper" ) ) {
            po.addLogFailed( String.format( "Node %s already has Zookeeper installed", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
            po.addLogFailed( String.format( "Node %s has no Hadoop installation", lxcHostname ) );
            return;
        }

        reconfigureCluster( lxcAgent, config );
    }


    private void reconfigureCluster( final Agent lxcAgent, final Config config ) {

        po.addLog( "Installing Zookeeper..." );

        //install
        Command installCommand = Commands.getInstallCommand( Util.wrapAgentToSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasCompleted() ) {
            po.addLog( "Installation succeeded\nReconfiguring cluster..." );

            config.getNodes().add( lxcAgent );

            //update settings
            Command updateSettingsCommand = Commands.getUpdateSettingsCommand( config.getNodes() );
            manager.getCommandRunner().runCommand( updateSettingsCommand );

            if ( updateSettingsCommand.hasSucceeded() ) {
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
                    po.addLog( "Cluster restarted successfully" );
                }
                else {
                    po.addLog( String.format( "Failed to restart cluster, %s, skipping...",
                            restartCommand.getAllErrors() ) );
                }

                po.addLog( "Updating information in database..." );

                try {
                    manager.getDbManager().saveInfo2( Config.PRODUCT_KEY, clusterName, config );

                    po.addLogDone( "Information in database updated successfully" );
                }
                catch ( DBException e ) {
                    po.addLogFailed( String.format( "Failed to update information in database, %s", e.getMessage() ) );
                }
            }
            else {
                po.addLogFailed(
                        String.format( "Failed to reconfigure cluster, %s", updateSettingsCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLogFailed( String.format( "Installation failed, %s\nUse Terminal/LXC Module to cleanup",
                    installCommand.getAllErrors() ) );
        }
    }


    private void addStandalone( final Config config ) {
        try {

            //create lxc
            po.addLog( "Creating lxc container..." );

            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs( 1 );

            Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

            po.addLog( "Lxc container created successfully" );

            reconfigureCluster( lxcAgent, config );
        }
        catch ( LxcCreateException ex ) {
            po.addLogFailed( ex.getMessage() );
        }
    }
}
