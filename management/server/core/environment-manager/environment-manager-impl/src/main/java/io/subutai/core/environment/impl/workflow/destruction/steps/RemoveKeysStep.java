package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RemoveKeysStep
{

    private final EnvironmentImpl environment;


    public RemoveKeysStep( final EnvironmentImpl environment )
    {
        this.environment = environment;
    }


    public void execute() throws PeerException
    {
        for ( final Peer peer : environment.getPeers() )
        {
            peer.removePeerEnvironmentKeyPair( environment.getEnvironmentId() );
        }
    }
}
