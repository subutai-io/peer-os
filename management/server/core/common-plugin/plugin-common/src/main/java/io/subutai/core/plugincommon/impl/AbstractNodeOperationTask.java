package io.subutai.core.plugincommon.impl;


import java.util.UUID;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperationView;
import io.subutai.core.plugincommon.api.CompleteEvent;
import io.subutai.core.plugincommon.api.ConfigBase;
import io.subutai.core.plugincommon.api.NodeOperationTaskInterface;
import io.subutai.core.plugincommon.api.NodeState;
import io.subutai.core.tracker.api.Tracker;


public abstract class AbstractNodeOperationTask implements Runnable, NodeOperationTaskInterface
{

    private final CompleteEvent completeEvent;
    private final Tracker tracker;
    private UUID trackID;
    private ConfigBase clusterConfig;
    private ContainerHost containerHost;


    public AbstractNodeOperationTask( Tracker tracker, ConfigBase clusterConfig, CompleteEvent completeEvent,
                                      UUID trackID, ContainerHost containerHost )
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
            while ( !Thread.interrupted() )
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
                        Thread.currentThread().interrupt();
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
            UUID theTrackID = runTask();
            waitUntilOperationFinish( theTrackID );
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
                    else if ( po.getLog().toLowerCase().contains( getProductRunningIdentifier().toLowerCase() ) )
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
                Thread.currentThread().interrupt();
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
            {
                break;
            }
        }

        completeEvent.onComplete( state );
    }
}

