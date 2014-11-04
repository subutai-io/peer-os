package org.safehaus.subutai.plugin.cassandra.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;


public class StopTask implements Runnable
{

    private final String clusterName;
    private final CompleteEvent completeEvent;
    private UUID containerId;
    private Cassandra cassandra;
    private Tracker tracker;


    public StopTask( Cassandra cassandra, Tracker tracker, String clusterName, UUID containerId,
                     CompleteEvent completeEvent )
    {
        this.cassandra = cassandra;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.containerId = containerId;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {

        UUID trackID = cassandra.stopService( clusterName, containerId );

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( CassandraClusterConfig.PRODUCT_KEY, trackID );
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
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
            {
                break;
            }
        }
    }
}
