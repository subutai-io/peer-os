package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class CleanupEnvironmentStep
{
    private final EnvironmentImpl environment;


    public CleanupEnvironmentStep( final EnvironmentImpl environment )
    {
        this.environment = environment;
    }


    public void execute() throws PeerException
    {
        for ( Peer peer : environment.getPeers() )
        {
            //todo run in a thread
            peer.cleanupEnvironment( environment.getEnvironmentId() );
        }
    }
}
