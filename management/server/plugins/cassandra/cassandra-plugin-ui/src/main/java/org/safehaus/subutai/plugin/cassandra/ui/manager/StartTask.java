package org.safehaus.subutai.plugin.cassandra.ui.manager;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.ui.CassandraUI;

import java.util.UUID;

public class StartTask implements Runnable {

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;

    public StartTask(String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

    @Override
    public void run() {

        UUID trackID = CassandraUI.getCassandraManager().startService( clusterName, hostname );

        long start = System.currentTimeMillis();
        while(!Thread.interrupted()) {
            ProductOperationView po = CassandraUI.getTracker().getProductOperation( CassandraClusterConfig.PRODUCT_KEY, trackID );
            if( po != null ) {
                if( po.getState() != ProductOperationState.RUNNING ) {
                    completeEvent.onComplete( po.getLog() );
                    break;
                }
            }
            try {
                Thread.sleep( 1000 );
            } catch( InterruptedException ex ) {
                break;
            }
            if( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 ) {
                break;
            }
        }
    }

}
