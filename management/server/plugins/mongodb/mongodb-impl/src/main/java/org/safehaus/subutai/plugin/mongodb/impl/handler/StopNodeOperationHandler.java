package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;


/**
 * Handles stop mongo node operation
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<MongoImpl, MongoClusterConfig>
{
    private final TrackerOperation po;
    private final String lxcHostname;


    public StopNodeOperationHandler( MongoImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createTrackerOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Stopping node %s in %s", lxcHostname, clusterName ) );
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


        po.addLog( "Stopping node..." );

        MongoNode node = config.findNode( lxcHostname );

        try
        {
            node.stop();
            po.addLogDone( String.format( "Node on %s stopped", lxcHostname ) );
        }
        catch ( Exception e )
        {
            po.addLogFailed( String.format( "Failed to stop node %s, %s", lxcHostname, e ) );
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
        //        po.addLog( "Stopping node..." );
        //        Command stopNodeCommand = manager.getCommands().getStopNodeCommand( Sets.newHashSet( node ) );
        //        manager.getCommandRunner().runCommand( stopNodeCommand );
        //
        //        if ( stopNodeCommand.hasSucceeded() )
        //        {
        //            po.addLogDone( String.format( "Node on %s stopped", lxcHostname ) );
        //        }
        //        else
        //        {
        //            po.addLogFailed(
        //                    String.format( "Failed to stop node %s, %s", lxcHostname, stopNodeCommand.getAllErrors
        // () ) );
        //        }
    }
}
