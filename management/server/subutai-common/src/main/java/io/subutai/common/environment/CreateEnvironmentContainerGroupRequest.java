package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.task.CloneRequest;


public class CreateEnvironmentContainerGroupRequest
{
    private final Set<CloneRequest> requests = new HashSet<>();
    private final String environmentId;


    public CreateEnvironmentContainerGroupRequest( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public void addRequest( final CloneRequest request )
    {
        if ( request == null )
        {
            throw new IllegalArgumentException( "Clone request could not be null." );
        }

        this.requests.add( request );
    }


    public Set<CloneRequest> getRequests()
    {
        return requests;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
