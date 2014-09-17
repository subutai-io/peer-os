package org.safehaus.subutai.plugin.elasticsearch.ui.manager;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.ui.ElasticsearchUI;

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

        UUID trackID = ElasticsearchUI.getElasticsearchManager().startNode( clusterName, hostname );

        long start = System.currentTimeMillis();
        while(!Thread.interrupted()) {
            ProductOperationView po = ElasticsearchUI.getTracker().getProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY, trackID );
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
