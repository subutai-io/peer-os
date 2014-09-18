package org.safehaus.subutai.plugin.elasticsearch.ui.manager;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import java.util.UUID;

public class StopTask implements Runnable {

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;
    private final Elasticsearch elasticsearch;
    private final Tracker tracker;

    public StopTask( Elasticsearch elasticsearch, Tracker tracker, String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.elasticsearch = elasticsearch;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

    @Override
    public void run() {

        UUID trackID = elasticsearch.stopNode( clusterName, hostname );

        long start = System.currentTimeMillis();
        while(!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY, trackID );
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
