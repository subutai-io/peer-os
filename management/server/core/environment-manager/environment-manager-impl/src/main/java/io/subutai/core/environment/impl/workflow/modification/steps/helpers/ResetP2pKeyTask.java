package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import java.util.concurrent.Callable;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;


public class ResetP2pKeyTask implements Callable<Object>
{

    private final Peer peer;
    private final P2PCredentials p2PCredentials;
    private final TrackerOperation trackerOperation;


    public ResetP2pKeyTask( final Peer peer, final P2PCredentials p2PCredentials,
                            final TrackerOperation trackerOperation )
    {
        this.peer = peer;
        this.p2PCredentials = p2PCredentials;
        this.trackerOperation = trackerOperation;
    }


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
            trackerOperation.addLog(
                    String.format( "Resetting p2p secret key on peer %s skipped due to peer status %s", peer.getId(),
                            status ) );
        }

        return null;
    }
}
