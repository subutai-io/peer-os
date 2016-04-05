package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class CleanupEnvironmentStep
{
    private static final Logger LOG = LoggerFactory.getLogger( CleanupEnvironmentStep.class );
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

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.cleanupEnvironment( environment.getEnvironmentId() );

                    return peer;
                }
            } );
        }

        executorService.shutdown();

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems cleaning up environment", e );
            }
        }


        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation
                    .addLog( String.format( "Environment cleanup succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "Environment cleanup failed on peer %s", failedPeer.getName() ) );
        }
    }
}
