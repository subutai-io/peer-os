package org.safehaus.subutai.plugin.cassandra.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;


public class StopTask implements Runnable {

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;
    private Manager manager;


    public StopTask( String clusterName, String lxcHostname, CompleteEvent completeEvent ) {
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.manager = manager;
    }


    @Override
    public void run() {

        UUID trackID = manager.getCassandraUI().getCassandraManager().stopService( clusterName, hostname );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() ) {
            ProductOperationView po = manager.getCassandraUI().getTracker()
                                             .getProductOperation( CassandraClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null ) {
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    completeEvent.onComplete( po.getLog() );
                    break;
                }
            }
            try {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex ) {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 ) {
                break;
            }
        }
    }
}
