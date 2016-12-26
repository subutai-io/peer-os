package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Set;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.PeerEnvironmentKeyTask;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


public class PEKGenerationStep
{
    private final Topology topology;
    private final LocalEnvironment environment;
    private final PeerManager peerManager;
    private final TrackerOperation trackerOperation;
    private final SecurityManager securityManager;
    protected PeerUtil<Object> pekUtil = new PeerUtil<>();


    public PEKGenerationStep( final Topology topology, final LocalEnvironment environment, final PeerManager peerManager,
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


        final PGPSecretKeyRing envSecKeyRing = getEnvironmentKeyRing();
        final PGPPublicKeyRing localPeerSignedPEK = getLocalPeerPek();


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


    protected PGPSecretKeyRing getEnvironmentKeyRing()
    {
        return securityManager.getKeyManager().getSecretKeyRing( environment.getEnvironmentId().getId() );
    }


    protected PGPPublicKeyRing getLocalPeerPek()
    {
        return securityManager.getKeyManager().getPublicKeyRing(
                peerManager.getLocalPeer().getId() + "_" + environment.getEnvironmentId().getId() );
    }
}
