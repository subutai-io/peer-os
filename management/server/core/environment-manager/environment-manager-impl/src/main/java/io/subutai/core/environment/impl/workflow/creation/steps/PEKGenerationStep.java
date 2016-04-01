package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.workflow.task.PeerEnvironmentKeyTask;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * PEK generation step
 */
public class PEKGenerationStep
{
    private static final Logger LOG = LoggerFactory.getLogger( PEKGenerationStep.class );

    private final Topology topology;
    private final Environment environment;
    private final PeerManager peerManager;
    private final SecurityManager securityManager;
    private final TrackerOperation trackerOperation;


    public PEKGenerationStep( final Topology topology, final Environment environment, final PeerManager peerManager,
                              SecurityManager securityManager, TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        PGPSecretKeyRing envSecKeyRing = getEnvironmentKeyRing();

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        peers.remove( peerManager.getLocalPeer() );

        // first creating PEK for local peer
        PGPPublicKeyRing localPeerSignedPEK;
        try
        {
            RelationLinkDto envLink = new RelationLinkDto( environment );
            PublicKeyContainer publicKeyContainer =
                    peerManager.getLocalPeer().createPeerEnvironmentKeyPair( envLink );

            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

            localPeerSignedPEK =
                    securityManager.getKeyManager().setKeyTrust( envSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

            peerManager.getLocalPeer()
                       .updatePeerEnvironmentPubKey( environment.getEnvironmentId(), localPeerSignedPEK );

            trackerOperation.addLog( "PEK generation succeeded on Local Peer" );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not create PEK for: " + peerManager.getLocalPeer().getId() );
        }

        if ( peers.isEmpty() )
        {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );


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
}
