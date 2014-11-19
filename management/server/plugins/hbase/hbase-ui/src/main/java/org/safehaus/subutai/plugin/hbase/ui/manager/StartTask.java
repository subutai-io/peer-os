package org.safehaus.subutai.plugin.hbase.ui.manager;


import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;


public class StartTask implements Runnable
{

    private static final Logger LOG = Logger.getLogger( StartTask.class.getName() );
    private final String clusterName;
    private final CompleteEvent completeEvent;
    private final HBase hbase;
    private final Tracker tracker;


    public StartTask( final HBase hBase, final Tracker tracker, String clusterName, CompleteEvent completeEvent )
    {
        this.clusterName = clusterName;
        this.completeEvent = completeEvent;
        this.hbase = hBase;
        this.tracker = tracker;
    }


    @Override
    public void run()
    {
        UUID trackID = hbase.startCluster( clusterName );

        long start = System.currentTimeMillis();

        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( HBaseConfig.PRODUCT_KEY, trackID );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
                {
                    /** Since start command does not return, we have wait here some time to update status columns of
                     * nodes correctly  */
                    try
                    {
                        Thread.sleep( 10000 );
                    }
                    catch ( InterruptedException e )
                    {
                        LOG.warning( "Waiting hbase services to start" );
                    }
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
            if ( System.currentTimeMillis() - start > 120 * 1000 )
            {
                break;
            }
        }
    }
}