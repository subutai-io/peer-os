package io.subutai.core.peer.impl.container;


import java.util.Set;

import io.subutai.common.peer.ContainerHost;


public class DestroyEnvironmentContainerGroupResponse
{
    private Set<ContainerHost> destroyedContainersIds;
    private String exception;


    public DestroyEnvironmentContainerGroupResponse( final Set<ContainerHost> destroyedContainersIds,
                                                     final String exception )
    {
        this.destroyedContainersIds = destroyedContainersIds;
        this.exception = exception;
    }


    public Set<ContainerHost> getDestroyedContainersIds()
    {
        return destroyedContainersIds;
    }


    public String getException()
    {
        return exception;
    }
}
