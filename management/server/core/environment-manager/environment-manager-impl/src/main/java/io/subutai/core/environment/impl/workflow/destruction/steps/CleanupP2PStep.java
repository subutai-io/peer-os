package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class CleanupP2PStep
{
    private final EnvironmentImpl environment;


    public CleanupP2PStep( final EnvironmentImpl environment )
    {
        this.environment = environment;
    }


    public void execute() throws PeerException
    {
        for ( Peer peer : environment.getPeers() )
        {
            peer.removeP2PConnection( environment.getEnvironmentId() );
        }
    }
}
