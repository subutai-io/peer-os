package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.EnvironmentDestructionException;
import org.safehaus.subutai.common.peer.EnvironmentDestructionResult;

import com.google.common.base.Preconditions;


public class EnvironmentDestructionResultImpl implements EnvironmentDestructionResult
{
    private final Set<UUID> destroyedContainersIds;
    private final EnvironmentDestructionException exception;


    public EnvironmentDestructionResultImpl( final Set<UUID> destroyedContainersIds,
                                             final EnvironmentDestructionException exception )
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
    public EnvironmentDestructionException getException()
    {
        return exception;
    }
}
