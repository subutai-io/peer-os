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
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;


/**
 * @author dilshat
 */
public class CheckTask implements Runnable
{

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Mongo mongo;
    private final Tracker tracker;


    public CheckTask( Mongo mongo, Tracker tracker, String clusterName, String lxcHostname,
                      CompleteEvent completeEvent )
    {
        this.mongo = mongo;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    public void run()
    {

        UUID trackID = mongo.checkNode( clusterName, lxcHostname );

        NodeState state = NodeState.UNKNOWN;
        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( MongoClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
                {
                    if ( po.getLog().contains( NodeState.STOPPED.toString() ) )
                    {
                        state = NodeState.STOPPED;
                    }
                    else if ( po.getLog().contains( NodeState.RUNNING.toString() ) )
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
            if ( System.currentTimeMillis() - start > ( Timeouts.CHECK_NODE_STATUS_TIMEOUT_SEC + 3 ) * 1000 )
            {
                break;
            }
        }
        completeEvent.onComplete( state );
    }
}
