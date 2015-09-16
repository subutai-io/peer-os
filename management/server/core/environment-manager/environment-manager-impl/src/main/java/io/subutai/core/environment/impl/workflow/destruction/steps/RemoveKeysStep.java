package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.Set;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.LocalPeer;


public class RemoveKeysStep
{

    private final EnvironmentImpl environment;
    private final LocalPeer localPeer;


    public RemoveKeysStep( final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.environment = environment;
        this.localPeer = localPeer;
    }


    public void execute() throws PeerException
    {

        Set<Peer> peers = environment.getPeers();
        peers.add( localPeer );

        for ( final Peer peer : peers )
        {
            peer.removeEnvironmentKeypair( environment.getId() );
        }
    }
}
