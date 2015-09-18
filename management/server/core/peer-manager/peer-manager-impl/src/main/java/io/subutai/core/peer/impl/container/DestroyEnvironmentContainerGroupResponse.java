package io.subutai.core.peer.impl.container;


import java.util.Set;


public class DestroyEnvironmentContainerGroupResponse
{
    private Set<String> destroyedContainersIds;
    private String exception;


    public DestroyEnvironmentContainerGroupResponse( final Set<String> destroyedContainersIds, final String exception )
    {
        this.destroyedContainersIds = destroyedContainersIds;
        this.exception = exception;
    }


    public Set<String> getDestroyedContainersIds()
    {
        return destroyedContainersIds;
    }


    public String getException()
    {
        return exception;
    }
}
