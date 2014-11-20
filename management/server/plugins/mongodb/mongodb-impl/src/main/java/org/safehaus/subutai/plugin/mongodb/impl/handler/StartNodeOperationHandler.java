package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;


/**
 * Handles start mongo node operation
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<MongoImpl, MongoClusterConfig>
{
    private final TrackerOperation po;
    private final String lxcHostname;


    public StartNodeOperationHandler( MongoImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createTrackerOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Starting node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        MongoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }


        po.addLog( "Starting node..." );

        MongoNode node = config.findNode( lxcHostname );

        try
        {
            node.start();
            po.addLogDone( String.format( "Node on %s started", lxcHostname ) );
        }
        catch ( Exception e )
        {
            po.addLogFailed( String.format( "Failed to start node %s, %s", lxcHostname, e ) );
        }

        //        MongoClusterConfig config = manager.getCluster( clusterName );
        //        if ( config == null )
        //        {
        //            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
        //            return;
        //        }
        //
        //        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        //        if ( node == null )
        //        {
        //            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
        //            return;
        //        }
        //        if ( !config.getAllNodes().contains( node ) )
        //        {
        //            po.addLogFailed(
        //                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname,
        // clusterName ) );
        //            return;
        //        }
        //
        //        Command startNodeCommand;
        //        NodeType nodeType = config.getNodeType( node );
        //
        //        if ( nodeType == NodeType.CONFIG_NODE )
        //        {
        //            startNodeCommand = manager.getCommands()
        //                                      .getStartConfigServerCommand( config.getCfgSrvPort(),
        // Sets.newHashSet( node ) );
        //        }
        //        else if ( nodeType == NodeType.DATA_NODE )
        //        {
        //            startNodeCommand =
        //                    manager.getCommands().getStartDataNodeCommand( config.getDataNodePort(),
        // Sets.newHashSet( node ) );
        //        }
        //        else
        //        {
        //            startNodeCommand = manager.getCommands()
        //                                      .getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(),
        //                                              config.getDomainName(), config.getConfigServers(),
        //                                              Sets.newHashSet( node ) );
        //        }
        //        po.addLog( "Starting node..." );
        //        manager.getCommandRunner().runCommand( startNodeCommand, new CommandCallback()
        //        {
        //
        //            @Override
        //            public void onResponse( Response response, AgentResult agentResult, Command command )
        //            {
        //                if ( agentResult.getStdOut().contains( "child process started successfully,
        // parent exiting" ) )
        //                {
        //
        //                    command.setData( NodeState.RUNNING );
        //
        //                    stop();
        //                }
        //            }
        //        } );
        //
        //        if ( NodeState.RUNNING.equals( startNodeCommand.getData() ) )
        //        {
        //            po.addLogDone( String.format( "Node on %s started", lxcHostname ) );
        //        }
        //        else
        //        {
        //            po.addLogFailed(
        //                    String.format( "Failed to start node %s. %s", lxcHostname, startNodeCommand
        // .getAllErrors() ) );
        //        }
    }
}
