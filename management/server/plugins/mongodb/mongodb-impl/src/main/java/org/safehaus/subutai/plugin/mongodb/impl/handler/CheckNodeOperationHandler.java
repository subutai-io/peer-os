package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;


/**
 * Handles check mongo node status operation
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<MongoImpl, MongoClusterConfig>
{
    private final TrackerOperation po;
    private final String lxcHostname;


    public CheckNodeOperationHandler( MongoImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createTrackerOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Checking state of %s in %s", lxcHostname, clusterName ) );
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


        MongoNode node = config.findNode( lxcHostname );
        if ( node == null )
        {
            po.addLogFailed( String.format( "Node on %s is not found", lxcHostname ) );
            return;
        }
        NodeState nodeState;
        if ( node.isRunning() )
        {
            nodeState = NodeState.RUNNING;
        }
        else
        {
            nodeState = NodeState.STOPPED;
        }
        po.addLogDone( String.format( "Node on %s is %s", lxcHostname, nodeState ) );

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
        //        po.addLog( "Checking node..." );
        //                Command checkNodeCommand = manager.getCommands().
        //                        getCheckInstanceRunningCommand( node, config.getDomainName(), config.getNodePort(
        // node ) );
        //        manager.getCommandRunner().runCommand( checkNodeCommand );
        //
        //        if ( checkNodeCommand.hasCompleted() )
        //        {
        //            AgentResult agentResult = checkNodeCommand.getResults().get( node.getUuid() );
        //            if ( agentResult != null )
        //            {
        //                if ( agentResult.getStdOut().contains( "couldn't connect to server" ) )
        //                {
        //                    po.addLogDone( String.format( "Node on %s is %s", lxcHostname, NodeState.STOPPED ) );
        //                }
        //                else if ( agentResult.getStdOut().contains( "connecting to" ) )
        //                {
        //                    po.addLogDone( String.format( "Node on %s is %s", lxcHostname, NodeState.RUNNING ) );
        //                }
        //                else
        //                {
        //                    po.addLogFailed( String.format( "Node on %s is not found", lxcHostname ) );
        //                }
        //                return;
        //            }
        //        }
        //        po.addLogFailed( String.format( "Error checking status of node %s : %s", node.getHostname(),
        //                checkNodeCommand.getAllErrors() ) );
    }
}
