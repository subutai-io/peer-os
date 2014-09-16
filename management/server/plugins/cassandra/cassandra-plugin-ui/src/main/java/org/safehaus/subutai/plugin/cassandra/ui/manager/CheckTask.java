/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;


public class CheckTask implements Runnable {

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private Manager manager;


    public CheckTask( String clusterName, String lxcHostname, CompleteEvent completeEvent ) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    public void run() {

        UUID trackID = manager.getCassandraUI().getCassandraManager().checkNode( clusterName, lxcHostname );

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
