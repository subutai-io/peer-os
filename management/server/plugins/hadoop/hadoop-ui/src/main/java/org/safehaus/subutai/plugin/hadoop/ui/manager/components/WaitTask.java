package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


/**
 * Created by daralbaev on 17.04.14.
 */
public class WaitTask implements Runnable
{
    private final CompleteEvent completeEvent;
    private final Tracker tracker;
    private UUID trackID;


    public WaitTask( Tracker tracker, UUID trackID, CompleteEvent completeEvent )
    {
        this.tracker = tracker;
        this.trackID = trackID;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {
        if ( trackID != null )
        {
            while ( true )
            {
                TrackerOperationView po = tracker.getTrackerOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
                if ( po.getState() == OperationState.RUNNING )
                {
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex )
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
        }
        NodeState state = NodeState.UNKNOWN;
        completeEvent.onComplete( state );
    }
}
