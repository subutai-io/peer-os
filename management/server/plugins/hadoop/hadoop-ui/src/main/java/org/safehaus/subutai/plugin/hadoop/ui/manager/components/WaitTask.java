package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
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
                ProductOperationView po = tracker.getProductOperation( HadoopClusterConfig.PRODUCT_KEY, trackID );
                if ( po.getState() == ProductOperationState.RUNNING )
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
