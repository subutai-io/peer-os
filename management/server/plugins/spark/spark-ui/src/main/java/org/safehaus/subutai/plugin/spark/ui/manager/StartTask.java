package org.safehaus.subutai.plugin.spark.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class StartTask implements Runnable
{

    private final boolean master;
    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Spark spark;
    private final Tracker tracker;


    public StartTask( final Spark spark, final Tracker tracker, String clusterName, String lxcHostname, boolean master,
                      CompleteEvent completeEvent )
    {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.master = master;
        this.spark = spark;
        this.tracker = tracker;
    }


    @Override
    public void run()
    {

        UUID trackID = spark.startNode( clusterName, lxcHostname, master );

        long start = System.currentTimeMillis();

        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( SparkClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != ProductOperationState.RUNNING )
                {
                    completeEvent.onComplete( po.getLog() );
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
            if ( System.currentTimeMillis() - start > 60 * 1000 )
            {
                break;
            }
        }
    }
}
