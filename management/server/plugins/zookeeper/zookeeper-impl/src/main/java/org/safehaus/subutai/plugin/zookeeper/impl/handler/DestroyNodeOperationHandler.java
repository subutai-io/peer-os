package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;


/**
 * Created by dilshat on 5/7/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final String lxcHostname;


    public DestroyNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
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

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( agent ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 ) {
            po.addLogFailed(
                    "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        //destroy lxc
        po.addLog( "Destroying lxc container..." );
        Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );
        if ( physicalAgent == null ) {
            po.addLog(
                    String.format( "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                            agent.getHostname() ) );
        }
        else {

            try {
                manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                po.addLog( "Lxc container destroyed successfully" );
            }
            catch ( LxcDestroyException e ) {
                po.addLog( String.format( "Could not destroy lxc container %s. Use LXC module to cleanup, skipping...",
                        e.getMessage() ) );
            }
        }

        config.getNodes().remove( agent );

        //update settings
        po.addLog( "Updating settings..." );
        Command updateSettingsCommand = Commands.getUpdateSettingsCommand( config.getZkName(), config.getNodes() );
        manager.getCommandRunner().runCommand( updateSettingsCommand );

        if ( updateSettingsCommand.hasSucceeded() ) {
            po.addLog( "Settings updated\nRestarting cluster..." );
            //restart all other nodes with new configuration
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
                po.addLog( "Cluster successfully restarted" );
            }
            else {
                po.addLog(
                        String.format( "Failed to restart cluster, %s, skipping...", restartCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLog( String.format(
                            "Settings update failed, %s\nPlease update settings manually and restart the cluster, " +
                                    "skipping...",
                            updateSettingsCommand.getAllErrors() ) );
        }

        //update db
        po.addLog( "Updating db..." );
        if ( !manager.getDbManager().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) ) {
            po.addLogFailed( String.format( "Error while updating cluster info [%s] in DB. Check logs\nFailed",
                    config.getClusterName() ) );
        }
        else {
            po.addLogDone( "Done" );
        }
    }
}
