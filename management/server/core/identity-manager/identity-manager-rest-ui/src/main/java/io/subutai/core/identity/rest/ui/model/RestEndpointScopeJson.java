package io.subutai.core.identity.rest.ui.model;

import com.google.common.base.Preconditions;
import io.subutai.core.identity.api.RestEndpointScope;

public class RestEndpointScopeJson implements RestEndpointScope
{

    private String restEndpoint = "rest_endpoint";

    private Long port;


    public RestEndpointScopeJson( final String restEndpoint )
    {
        Preconditions.checkNotNull(restEndpoint, "Invalid argument restEndpoint");
        this.restEndpoint = restEndpoint;
    }


    @Override
    public String getRestEndpoint()
    {
        return restEndpoint;
    }


    @Override
    public Long getPort()
    {
        return port;
    }
}
