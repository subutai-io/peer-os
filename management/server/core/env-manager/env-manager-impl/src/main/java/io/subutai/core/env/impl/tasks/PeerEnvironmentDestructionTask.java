package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Peer;


/**
 * PeerEnvironmentDestructionTask destroys Environment on Peer
 *
 * @see io.subutai.common.peer.Peer
 * @see io.subutai.common.environment.Environment
 */
public class PeerEnvironmentDestructionTask implements Callable<ContainersDestructionResult>
{
    private final Peer peer;
    private final String environmentId;


    public PeerEnvironmentDestructionTask( final Peer peer, final String environmentId )
    {
        Preconditions.checkNotNull( peer );
        Preconditions.checkNotNull( environmentId );

        this.peer = peer;
        this.environmentId = environmentId;
    }


    @Override
    public ContainersDestructionResult call() throws Exception
    {
        return peer.destroyEnvironmentContainers( environmentId );
    }
}
