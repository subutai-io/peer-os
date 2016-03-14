package io.subutai.core.environment.impl.workflow.modification.steps;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class P2PSecretKeyResetStep
{
    private final EnvironmentImpl environment;
    private final P2PCredentials p2PCredentials;


    public P2PSecretKeyResetStep( final EnvironmentImpl environment, final P2PCredentials p2PCredentials )
    {
        this.environment = environment;
        this.p2PCredentials = p2PCredentials;
    }


    public void execute() throws PeerException
    {
        for ( Peer peer : environment.getPeers() )
        {
            peer.resetP2PSecretKey( p2PCredentials );
        }
    }
}
