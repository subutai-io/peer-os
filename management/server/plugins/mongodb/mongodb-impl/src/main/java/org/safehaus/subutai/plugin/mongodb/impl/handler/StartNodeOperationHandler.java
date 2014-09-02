package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.enums.NodeState;

import com.google.common.collect.Sets;


/**
 * Handles start mongo node operation
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final String lxcHostname;


    public StartNodeOperationHandler( MongoImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Starting node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        MongoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null ) {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !config.getAllNodes().contains( node ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command startNodeCommand;
        NodeType nodeType = config.getNodeType( node );

        if ( nodeType == NodeType.CONFIG_NODE ) {
            startNodeCommand = Commands.getStartConfigServerCommand( config.getCfgSrvPort(), Sets.newHashSet( node ) );
        }
        else if ( nodeType == NodeType.DATA_NODE ) {
            startNodeCommand = Commands.getStartDataNodeCommand( config.getDataNodePort(), Sets.newHashSet( node ) );
        }
        else {
            startNodeCommand = Commands.getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(),
                    config.getDomainName(), config.getConfigServers(), Sets.newHashSet( node ) );
        }
        po.addLog( "Starting node..." );
        manager.getCommandRunner().runCommand( startNodeCommand, new CommandCallback() {

            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                if ( agentResult.getStdOut().contains( "child process started successfully, parent exiting" ) ) {

                    command.setData( NodeState.RUNNING );

                    stop();
                }
            }
        } );

        if ( NodeState.RUNNING.equals( startNodeCommand.getData() ) ) {
            po.addLogDone( String.format( "Node on %s started", lxcHostname ) );
        }
        else {
            po.addLogFailed(
                    String.format( "Failed to start node %s. %s", lxcHostname, startNodeCommand.getAllErrors() ) );
        }
    }
}
