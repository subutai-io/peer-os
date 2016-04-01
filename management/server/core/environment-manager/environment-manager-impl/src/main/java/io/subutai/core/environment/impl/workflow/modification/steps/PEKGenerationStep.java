package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.task.PeerEnvironmentKeyTask;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


public class PEKGenerationStep
{
    private static final Logger LOG = LoggerFactory.getLogger( PEKGenerationStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final TrackerOperation trackerOperation;
    private final SecurityManager securityManager;


    public PEKGenerationStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                              final SecurityManager securityManager, final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
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


        final PGPSecretKeyRing envSecKeyRing = getEnvironmentKeyRing();
        final PGPPublicKeyRing localPeerSignedPEK = getLocalPeerPek();
        // creating PEK on remote peers
        for ( final Peer peer : peers )
        {
            completionService.submit(
                    new PeerEnvironmentKeyTask( peerManager.getLocalPeer(), envSecKeyRing, localPeerSignedPEK,
                            environment, peer, securityManager.getKeyManager() ) );
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


    private PGPSecretKeyRing getEnvironmentKeyRing()
    {
        return securityManager.getKeyManager().getSecretKeyRing( environment.getEnvironmentId().getId() );
    }


    public PGPPublicKeyRing getLocalPeerPek()
    {
        return securityManager.getKeyManager().getPublicKeyRing(
                peerManager.getLocalPeer().getId() + "_" + environment.getEnvironmentId().getId() );
    }
}
