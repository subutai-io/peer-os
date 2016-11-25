package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class CleanupEnvironmentStep
{
    private final LocalEnvironment environment;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> cleanupUtil = new PeerUtil<>();


    public CleanupEnvironmentStep( final LocalEnvironment environment, final TrackerOperation trackerOperation )
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

        for ( PeerUtil.PeerTaskResult cleanupResult : cleanupResults.getResults() )
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
