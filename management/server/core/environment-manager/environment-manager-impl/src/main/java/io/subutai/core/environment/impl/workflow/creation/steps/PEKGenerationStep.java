package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.workflow.PeerUtil;
import io.subutai.core.environment.impl.workflow.task.PeerEnvironmentKeyTask;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * PEK generation step
 */
public class PEKGenerationStep
{
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
            PublicKeyContainer publicKeyContainer =
                    peerManager.getLocalPeer().createPeerEnvironmentKeyPair( environment.getEnvironmentId() );

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

        PeerUtil<Peer> pekUtil = new PeerUtil<>();

        // creating PEK on remote peers
        for ( final Peer peer : peers )
        {
            pekUtil.addPeerTask( new PeerUtil.PeerTask<>( peer,
                    new PeerEnvironmentKeyTask( peerManager.getLocalPeer(), envSecKeyRing, localPeerSignedPEK,
                            environment, peer, securityManager.getKeyManager() ) ) );
        }

        Set<PeerUtil.PeerTaskResult<Peer>> pekResults = pekUtil.executeParallel();

        boolean hasFailures = false;

        for ( PeerUtil.PeerTaskResult pekResult : pekResults )
        {
            if ( pekResult.hasSucceeded() )
            {
                trackerOperation.addLog(
                        String.format( "PEK generation succeeded on peer %s", pekResult.getPeer().getName() ) );
            }
            else
            {
                hasFailures = true;

                trackerOperation.addLog(
                        String.format( "PEK generation failed on peer %s. Reason: %s", pekResult.getPeer().getName(),
                                pekResult.getFailureReason() ) );
            }
        }

        if ( hasFailures )
        {
            throw new PeerException( "Failed to generate PEK across all peers" );
        }
    }


    private PGPSecretKeyRing getEnvironmentKeyRing()
    {
        return securityManager.getKeyManager().getSecretKeyRing( environment.getEnvironmentId().getId() );
    }
}
