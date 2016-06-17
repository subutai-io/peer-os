package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
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

        PeerUtil<Object> resetUtil = new PeerUtil<>();

        for ( final Peer peer : peers )
        {
            resetUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    final RegistrationStatus status = peer.getStatus();
                    if ( status == RegistrationStatus.APPROVED )
                    {
                        peer.resetSwarmSecretKey( p2PCredentials );
                    }
                    else
                    {
                        LOG.warn( String.format( "Resetting p2p secret key on peer %s skipped due to peer status %s",
                                peer.getId(), status ) );
                    }

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> resetResults = resetUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult resetResult : resetResults.getPeerTaskResults() )
        {
            if ( resetResult.hasSucceeded() )
            {
                trackerOperation.addLog(
                        String.format( "P2P secret key reset succeeded on peer %s", resetResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "P2P secret key reset failed on peer %s. Reason: %s",
                        resetResult.getPeer().getName(), resetResult.getFailureReason() ) );
            }
        }

        if ( resetResults.hasFailures() )
        {
            throw new PeerException( "Failed to reset p2p key across all peers" );
        }
    }
}
