package org.safehaus.subutai.plugin.spark.ui.manager;

import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class CheckTaskMaster implements Runnable {

    private final String clusterName, lxcHostname;
    private final boolean master;
    private final CompleteEvent completeEvent;
    private final Spark spark;
    private final Tracker tracker;


    public CheckTaskMaster( final Spark spark, final Tracker tracker, String clusterName, String lxcHostname, boolean master,
                           CompleteEvent completeEvent ) {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.master = master;
        this.spark = spark;
        this.tracker = tracker;
    }


    @Override
    public void run() {
        UUID trackID = spark.checkMasterNode( clusterName, lxcHostname );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() ) {
            ProductOperationView po = tracker.getProductOperation( SparkClusterConfig.PRODUCT_KEY, trackID );
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
            if ( System.currentTimeMillis() - start > 30 * 1000 ) {
                break;
            }
        }
    }
}
