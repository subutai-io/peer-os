package org.safehaus.subutai.plugin.presto.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;


public class StartTask implements Runnable {

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;
    private final Tracker tracker;
    private final Presto presto;


    public StartTask( final Presto presto, final Tracker tracker, String clusterName, String lxcHostname,
                      CompleteEvent completeEvent ) {
        this.presto = presto;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run() {

        UUID trackID = presto.startNode( clusterName, hostname );

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while ( !Thread.interrupted() ) {
            ProductOperationView po = tracker.getProductOperation( PrestoClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null ) {
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    if ( po.getState() == ProductOperationState.SUCCEEDED ) {
                        state = NodeState.RUNNING;
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
            if ( System.currentTimeMillis() - start > 60 * 1000 ) {
                break;
            }
        }

        completeEvent.onComplete( state );
    }
}
