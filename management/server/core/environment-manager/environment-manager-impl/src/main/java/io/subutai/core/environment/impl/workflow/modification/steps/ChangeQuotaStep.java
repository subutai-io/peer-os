package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.concurrent.Callable;

import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class ChangeQuotaStep
{

    private final EnvironmentImpl environment;
    private final Map<String, ContainerSize> changedContainers;
    private final TrackerOperation trackerOperation;


    public ChangeQuotaStep( final EnvironmentImpl environment, final Map<String, ContainerSize> changedContainers,
                            final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.changedContainers = changedContainers;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        if ( !CollectionUtil.isMapEmpty( changedContainers ) )
        {
            PeerUtil<Object> quotaUtil = new PeerUtil<>();


            for ( final Map.Entry<String, ContainerSize> entry : changedContainers.entrySet() )
            {
                final EnvironmentContainerHost containerHost = environment.getContainerHostById( entry.getKey() );
                final Peer peer = containerHost.getPeer();
                final ContainerSize containerSize = entry.getValue();

                quotaUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        peer.setContainerSize( containerHost.getContainerId(), containerSize );

                        return null;
                    }
                } ) );

                PeerUtil.PeerTaskResults<Object> quotaResults = quotaUtil.executeParallel();

                for ( PeerUtil.PeerTaskResult quotaResult : quotaResults.getPeerTaskResults() )
                {
                    if ( quotaResult.hasSucceeded() )
                    {
                        trackerOperation.addLog(
                                String.format( "Container %s's size has been modified", containerHost.getHostname() ) );
                    }
                    else
                    {
                        trackerOperation.addLog( String.format( "Failed to modify size of container %s. Reason: %s",
                                containerHost.getHostname(), quotaResult.getFailureReason() ) );
                    }
                }

                if ( quotaResults.hasFailures() )
                {
                    throw new PeerException( "Failed to change size of each container" );
                }
            }
        }
    }
}
