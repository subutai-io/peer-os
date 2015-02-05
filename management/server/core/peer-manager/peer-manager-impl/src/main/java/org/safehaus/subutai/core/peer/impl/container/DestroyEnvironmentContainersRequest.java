package org.safehaus.subutai.core.peer.impl.container;


import java.util.UUID;


public class DestroyEnvironmentContainersRequest
{
    private final UUID environmentId;


    public DestroyEnvironmentContainersRequest( final UUID environmentId )
    {

        this.environmentId = environmentId;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }
}
