/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;


/**
 * @author dilshat
 */
public class StopTask implements Runnable {

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Mongo mongo;
    private final Tracker tracker;


    public StopTask( Mongo mongo, Tracker tracker, String clusterName, String lxcHostname,
                     CompleteEvent completeEvent ) {
        this.mongo = mongo;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    public void run() {

        UUID trackID = mongo.stopNode( clusterName, lxcHostname );

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while ( !Thread.interrupted() ) {
            ProductOperationView po = tracker.getProductOperation( MongoClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null ) {
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    if ( po.getState() == ProductOperationState.SUCCEEDED ) {
                        state = NodeState.STOPPED;
                    }
                    break;
                }
            }
            try {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex ) {
                break;
            }
            if ( System.currentTimeMillis() - start > ( Timeouts.STOP_NODE_TIMEOUT_SEC + 3 ) * 1000 ) {
                break;
            }
        }

        completeEvent.onComplete( state );
    }
}
