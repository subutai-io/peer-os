package org.safehaus.subutai.plugin.common.impl;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationTaskInterface;


public abstract class AbstractNodeOperationTask implements Runnable, NodeOperationTaskInterface
{

    private final CompleteEvent completeEvent;
    private final Tracker tracker;
    private UUID trackID;
    private ConfigBase clusterConfig;
    private ContainerHost containerHost;

    public AbstractNodeOperationTask( Tracker tracker, ConfigBase clusterConfig,
                                      CompleteEvent completeEvent, UUID trackID, ContainerHost containerHost )
    {
        this.tracker = tracker;
        this.completeEvent = completeEvent;
        this.trackID = trackID;
        this.clusterConfig = clusterConfig;
        this.containerHost = containerHost;
    }


    public void run()
    {

        if ( trackID != null )
        {
            while ( true )
            {
                TrackerOperationView prevPo = tracker.getTrackerOperation( clusterConfig.getProductKey(), trackID );
                if ( prevPo.getState() == OperationState.RUNNING )
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

        if ( containerHost != null )
        {
            UUID trackID = runTask();
            waitUntilOperationFinish( trackID );
        }
    }


    public void waitUntilOperationFinish( UUID trackID )
    {
        NodeState state = NodeState.UNKNOWN;
        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( clusterConfig.getProductKey(), trackID );
            if ( po != null )
            {
                if ( po.getState() != OperationState.RUNNING )
                {
                    if ( po.getLog().toLowerCase().contains( getProductStoppedIdentifier().toLowerCase() ) )
                    {
                        state = NodeState.STOPPED;
                    }
                    else if ( po.getLog().toLowerCase()
                                .contains( getProductRunningIdentifier().toLowerCase() ) )
                    {
                        state = NodeState.RUNNING;
                    }
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

        completeEvent.onComplete( state );
    }
}

