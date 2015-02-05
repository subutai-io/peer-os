package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerDestructionException;
import org.safehaus.subutai.common.peer.ContainerDestructionResult;

import com.google.common.base.Preconditions;


public class ContainerDestructionResultImpl implements ContainerDestructionResult
{
    private final Set<UUID> destroyedContainersIds;
    private final ContainerDestructionException exception;


    public ContainerDestructionResultImpl( final Set<UUID> destroyedContainersIds,
                                           final ContainerDestructionException exception )
    {
        Preconditions.checkNotNull( destroyedContainersIds );

        this.destroyedContainersIds = destroyedContainersIds;
        this.exception = exception;
    }


    @Override
    public Set<UUID> getDestroyedContainersIds()
    {
        return destroyedContainersIds;
    }


    @Override
    public ContainerDestructionException getException()
    {
        return exception;
    }
}
