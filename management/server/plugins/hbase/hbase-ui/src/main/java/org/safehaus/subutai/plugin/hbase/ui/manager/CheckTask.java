package org.safehaus.subutai.plugin.hbase.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;


public class CheckTask implements Runnable
{

    private final String clusterName;
    private final UUID hostId;
    private final CompleteEvent completeEvent;
    private final HBase hbase;
    private final Tracker tracker;


    public CheckTask( final HBase hbase, final Tracker tracker, String clusterName, UUID hostId,
                      CompleteEvent completeEvent )
    {
        this.clusterName = clusterName;
        this.hostId = hostId;
        this.completeEvent = completeEvent;
        this.hbase = hbase;
        this.tracker = tracker;
    }


    @Override
    public void run()
    {
        UUID trackID = hbase.checkNode( clusterName, hostId );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( HBaseConfig.PRODUCT_KEY, trackID );
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

            if ( System.currentTimeMillis() - start > 60 * 1000 )
            {
                break;
            }
        }
    }
}
