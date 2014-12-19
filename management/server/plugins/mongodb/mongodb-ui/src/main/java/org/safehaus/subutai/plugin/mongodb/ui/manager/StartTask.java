/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;


/**
 * @author dilshat
 */
public class StartTask implements Runnable
{

    private final NodeType nodeType;
    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Mongo mongo;
    private final Tracker tracker;


    public StartTask( Mongo mongo, Tracker tracker, NodeType nodeType, String clusterName, String lxcHostname,
                      CompleteEvent completeEvent )
    {
        this.mongo = mongo;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.nodeType = nodeType;
    }


    public void run()
    {

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;
        int waitTimeout = Timeouts.START_DATE_NODE_TIMEOUT_SEC;
        if ( nodeType == NodeType.CONFIG_NODE )
        {
            waitTimeout = Timeouts.START_CONFIG_SERVER_TIMEOUT_SEC;
        }
        else if ( nodeType == NodeType.ROUTER_NODE )
        {

            waitTimeout = Timeouts.START_ROUTER_TIMEOUT_SEC;

            //            Set<MongoConfigNode> list = mongo.getCluster( clusterName ).getConfigServers();
            //            for ( MongoConfigNode node : list ){
            //                try
            //                {
            //                    node.start();
            //                }
            //                catch ( MongoException e )
            //                {
            //                    e.printStackTrace();
            //                }
            //
            //                UUID track = mongo.checkNode( clusterName, node.getHostname() );
            //
            //                while ( !Thread.interrupted() )
            //                {
            //                    TrackerOperationView po = tracker.getTrackerOperation( MongoClusterConfig
            // .PRODUCT_KEY, track );
            //                    if ( po != null )
            //                    {
            //                        if ( po.getState() != OperationState.RUNNING )
            //                        {
            //                            if ( po.getState() == OperationState.SUCCEEDED )
            //                            {
            //                                state = NodeState.RUNNING;
            //                            }
            //                            break;
            //                        }
            //                    }
            //                    try
            //                    {
            //                        Thread.sleep( 1000 );
            //                    }
            //                    catch ( InterruptedException ex )
            //                    {
            //                        break;
            //                    }
            //                    if ( System.currentTimeMillis() - start > ( waitTimeout + 3 ) * 1000 )
            //                    {
            //                        break;
            //                    }
            //                }
            //            }
        }

        UUID trackID = mongo.startNode( clusterName, lxcHostname );

        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( MongoClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
                {
                    if ( po.getState() == OperationState.SUCCEEDED )
                    {
                        state = NodeState.RUNNING;
                    }
                    break;
                }
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
            if ( System.currentTimeMillis() - start > ( waitTimeout + 3 ) * 1000 )
            {
                break;
            }
        }

        completeEvent.onComplete( state );
    }
}
