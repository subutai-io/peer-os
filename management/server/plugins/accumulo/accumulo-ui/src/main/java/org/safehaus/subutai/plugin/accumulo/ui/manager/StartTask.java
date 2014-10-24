package org.safehaus.subutai.plugin.accumulo.ui.manager;


import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;


public class StartTask implements Runnable
{

    private static final Logger LOG = Logger.getLogger( StartTask.class.getName() );
    private final String clusterName;
    private final CompleteEvent completeEvent;
    private final Accumulo accumulo;
    private final Tracker tracker;


    public StartTask( final Accumulo accumulo1, final Tracker tracker, String clusterName, CompleteEvent completeEvent )
    {
        this.clusterName = clusterName;
        this.completeEvent = completeEvent;
        this.accumulo = accumulo1;
        this.tracker = tracker;
    }


    @Override
    public void run()
    {
        UUID trackID = accumulo.startCluster( clusterName );

        long start = System.currentTimeMillis();

        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY, trackID );
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
            if ( System.currentTimeMillis() - start > 120 * 1000 )
            {
                break;
            }
        }
    }
}