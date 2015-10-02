package io.subutai.core.environment.impl.workflow.destruction.steps.helpers;


import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Peer;


public class EnvironmentContainerGroupDestructionTask implements Callable<ContainersDestructionResult>
{
    private final Peer peer;
    private final String environmentId;


    public EnvironmentContainerGroupDestructionTask( final Peer peer, final String environmentId )
    {
        Preconditions.checkNotNull( peer );
        Preconditions.checkNotNull( environmentId );

        this.peer = peer;
        this.environmentId = environmentId;
    }


    @Override
    public ContainersDestructionResult call() throws Exception
    {
        return peer.destroyEnvironmentContainerGroup( environmentId );
    }
}
