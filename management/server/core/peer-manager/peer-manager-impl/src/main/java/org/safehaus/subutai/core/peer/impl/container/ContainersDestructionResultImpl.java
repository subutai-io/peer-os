package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainersDestructionResult;

import com.google.common.base.Preconditions;


public class ContainersDestructionResultImpl implements ContainersDestructionResult
{
    private final Set<UUID> destroyedContainersIds;
    private final String exception;


    public ContainersDestructionResultImpl( final Set<UUID> destroyedContainersIds, final String exception )
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
    public String getException()
    {
        return exception;
    }
}
