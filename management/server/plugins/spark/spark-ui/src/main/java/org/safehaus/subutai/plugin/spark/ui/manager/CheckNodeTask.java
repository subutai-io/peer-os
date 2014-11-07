package org.safehaus.subutai.plugin.spark.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class CheckNodeTask implements Runnable
{

    private final String clusterName, lxcHostname;
    private final CompleteEvent completeEvent;
    private final Spark spark;
    private final Tracker tracker;
    private final boolean master;


    public CheckNodeTask( final Spark spark, final Tracker tracker, String clusterName, String lxcHostname,
                          CompleteEvent completeEvent, boolean master )
    {
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
        this.spark = spark;
        this.tracker = tracker;
        this.master = master;
    }


    @Override
    public void run()
    {
        UUID trackID = spark.checkNode( clusterName, lxcHostname, master );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( SparkClusterConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
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

            if ( System.currentTimeMillis() - start > 30 * 1000 )
            {
                break;
            }
        }
    }
}
