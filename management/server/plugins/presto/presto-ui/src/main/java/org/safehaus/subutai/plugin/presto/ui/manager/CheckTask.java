package org.safehaus.subutai.plugin.presto.ui.manager;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;


public class CheckTask implements Runnable
{

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;
    private final Tracker tracker;
    private final Presto presto;


    public CheckTask( final Presto presto, final Tracker tracker, String clusterName, String lxcHostname,
                      CompleteEvent completeEvent )
    {
        this.presto = presto;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {

        UUID trackID = presto.checkNode( clusterName, hostname );
        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( PrestoClusterConfig.PRODUCT_KEY, trackID );
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
