package io.subutai.core.peer.impl.container;


public class DestroyEnvironmentContainerGroupRequest
{
    private final String environmentId;


    public DestroyEnvironmentContainerGroupRequest( final String environmentId )
    {

        this.environmentId = environmentId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
