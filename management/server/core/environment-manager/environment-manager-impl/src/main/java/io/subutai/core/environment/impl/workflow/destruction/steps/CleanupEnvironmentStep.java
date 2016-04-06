package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.PeerUtil;


public class CleanupEnvironmentStep
{
    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;


    public CleanupEnvironmentStep( final EnvironmentImpl environment, final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        Set<Peer> peers = environment.getPeers();

        if ( peers.isEmpty() )
        {
            return;
        }

        PeerUtil<Object> cleanupUtil = new PeerUtil<>();

        for ( final Peer peer : peers )
        {
            cleanupUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.cleanupEnvironment( environment.getEnvironmentId() );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> cleanupResults = cleanupUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult cleanupResult : cleanupResults.getPeerTaskResults() )
        {
            if ( cleanupResult.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Environment cleanup succeeded on peer %s",
                        cleanupResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Environment cleanup failed on peer %s. Reason: %s",
                        cleanupResult.getPeer().getName(), cleanupResult.getFailureReason() ) );
            }
        }
    }
}
