package io.subutai.core.environment.impl.workflow.modification.steps;


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
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class P2PSecretKeyResetStep
{
    private static final Logger LOG = LoggerFactory.getLogger( P2PSecretKeyResetStep.class );
    private final EnvironmentImpl environment;
    private final P2PCredentials p2PCredentials;
    private final TrackerOperation trackerOperation;


    public P2PSecretKeyResetStep( final EnvironmentImpl environment, final P2PCredentials p2PCredentials,
                                  final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.p2PCredentials = p2PCredentials;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        Set<Peer> peers = environment.getPeers();
        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );


        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.resetP2PSecretKey( p2PCredentials );

                    return peer;
                }
            } );
        }

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
                LOG.error( "Problems resetting p2p key", e );
            }
        }


        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation
                    .addLog( String.format( "P2P secret key reset succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "P2P secret key reset failed on peer %s", failedPeer.getName() ) );
        }

        if ( !peers.isEmpty() )
        {
            throw new PeerException( "Failed to reset p2p key across all peers" );
        }
    }
}
