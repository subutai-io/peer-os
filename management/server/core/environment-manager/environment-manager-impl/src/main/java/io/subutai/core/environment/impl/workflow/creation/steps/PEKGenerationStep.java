package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.PeerEnvironmentKeyTask;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * PEK generation step
 *
 * TODO refactor - split into smaller methods
 */
public class PEKGenerationStep
{
    private final Topology topology;
    private final Environment environment;
    private final PeerManager peerManager;
    private final SecurityManager securityManager;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> pekUtil = new PeerUtil<>();


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
            PublicKeyContainer publicKeyContainer = peerManager.getLocalPeer().createPeerEnvironmentKeyPair( envLink );

            PGPPublicKeyRing pubRing = readPublicKeyRing( publicKeyContainer );

            localPeerSignedPEK =
                    securityManager.getKeyManager().setKeyTrust( envSecKeyRing, pubRing, KeyTrustLevel.FULL.getId() );

            peerManager.getLocalPeer()
                       .updatePeerEnvironmentPubKey( environment.getEnvironmentId(), localPeerSignedPEK );

            trackerOperation.addLog( "PEK generation succeeded on Local Peer" );
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Could not create PEK for %s: %s", peerManager.getLocalPeer().getId(),
                            e.getMessage() ) );
        }

        if ( peers.isEmpty() )
        {
            return;
        }


        // creating PEK on remote peers
        for ( final Peer peer : peers )
        {
            pekUtil.addPeerTask( new PeerUtil.PeerTask<>( peer,
                    new PeerEnvironmentKeyTask( peerManager.getLocalPeer(), envSecKeyRing, localPeerSignedPEK,
                            environment, peer, securityManager.getKeyManager() ) ) );
        }

        PeerUtil.PeerTaskResults<Object> pekResults = pekUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult pekResult : pekResults.getResults() )
        {
            if ( pekResult.hasSucceeded() )
            {
                trackerOperation.addLog(
                        String.format( "PEK generation succeeded on peer %s", pekResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog(
                        String.format( "PEK generation failed on peer %s. Reason: %s", pekResult.getPeer().getName(),
                                pekResult.getFailureReason() ) );
            }
        }

        if ( pekResults.hasFailures() )
        {
            throw new PeerException( "Failed to generate PEK across all peers" );
        }
    }


    protected PGPPublicKeyRing readPublicKeyRing( final PublicKeyContainer publicKeyContainer ) throws PGPException
    {
        return PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );
    }


    protected PGPSecretKeyRing getEnvironmentKeyRing()
    {
        return securityManager.getKeyManager().getSecretKeyRing( environment.getEnvironmentId().getId() );
    }
}
