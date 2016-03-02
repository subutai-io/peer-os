package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class CleanUpNetworkStep
{
    private final EnvironmentImpl environment;


    public CleanUpNetworkStep( final EnvironmentImpl environment )
    {
        this.environment = environment;
    }


    public void execute() throws PeerException
    {
        for ( final Peer peer : environment.getPeers() )
        {
            peer.cleanupEnvironmentNetworkSettings( environment.getEnvironmentId() );
        }
    }
}
