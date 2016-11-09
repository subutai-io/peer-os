package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Set;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.modification.steps.helpers.ResetP2pKeyTask;


public class P2PSecretKeyResetStep
{

    private final EnvironmentImpl environment;
    private final P2PCredentials p2PCredentials;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> resetUtil = new PeerUtil<>();


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

        for ( final Peer peer : peers )
        {
            resetUtil.addPeerTask(
                    new PeerUtil.PeerTask<>( peer, new ResetP2pKeyTask( peer, p2PCredentials, trackerOperation ) ) );
        }

        PeerUtil.PeerTaskResults<Object> resetResults = resetUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult resetResult : resetResults.getResults() )
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
