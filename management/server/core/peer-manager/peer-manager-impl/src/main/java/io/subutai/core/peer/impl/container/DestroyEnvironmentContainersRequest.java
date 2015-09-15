package io.subutai.core.peer.impl.container;


public class DestroyEnvironmentContainersRequest
{
    private final String environmentId;


    public DestroyEnvironmentContainersRequest( final String environmentId )
    {

        this.environmentId = environmentId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
