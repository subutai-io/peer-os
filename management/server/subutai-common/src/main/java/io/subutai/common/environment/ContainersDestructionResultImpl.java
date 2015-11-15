package io.subutai.common.environment;


import java.util.Set;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainersDestructionResult;


public class ContainersDestructionResultImpl implements ContainersDestructionResult
{
    private final String peerId;
    private final Set<ContainerHost> destroyedContainersIds;
    private final String exception;


    public ContainersDestructionResultImpl( final String peerId, final Set<ContainerHost> destroyedContainersIds,
                                            final String exception )
    {
        Preconditions.checkNotNull( peerId );
        Preconditions.checkNotNull( destroyedContainersIds );

        this.peerId = peerId;
        this.destroyedContainersIds = destroyedContainersIds;
        this.exception = exception;
    }


    @Override
    public String peerId()
    {
        return peerId;
    }


    @Override
    public Set<ContainerHost> getDestroyedContainersIds()
    {
        return destroyedContainersIds;
    }


    @Override
    public String getException()
    {
        return exception;
    }
}
