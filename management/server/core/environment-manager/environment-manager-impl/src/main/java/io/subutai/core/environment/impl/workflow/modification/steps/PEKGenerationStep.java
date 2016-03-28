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

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.PeerManager;


//todo add trust relations
public class PEKGenerationStep
{
    private static final Logger LOG = LoggerFactory.getLogger( PEKGenerationStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final TrackerOperation trackerOperation;


    public PEKGenerationStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                              final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        //remove already existing peers
        peers.removeAll( environment.getPeers() );
        peers.remove( peerManager.getLocalPeer() );

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
                    peer.createPeerEnvironmentKeyPair( environment.getEnvironmentId() ).getKey();

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
                LOG.error( "Problems generating PEK", e );
            }
        }


        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "PEK generation succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "PEK generation failed on peer %s", failedPeer.getName() ) );
        }

        if ( !peers.isEmpty() )
        {
            throw new PeerException( "Failed to generate PEK across all peers" );
        }
    }
}
