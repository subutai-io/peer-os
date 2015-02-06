package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;
import java.util.UUID;


public class DestroyEnvironmentContainersResponse
{
    private Set<UUID> destroyedContainersIds;
    private String exception;


    public DestroyEnvironmentContainersResponse( final Set<UUID> destroyedContainersIds, final String exception )
    {
        this.destroyedContainersIds = destroyedContainersIds;
        this.exception = exception;
    }


    public Set<UUID> getDestroyedContainersIds()
    {
        return destroyedContainersIds;
    }


    public String getException()
    {
        return exception;
    }
}
