package org.safehaus.subutai.core.hostregistry.rest;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;

import com.google.common.base.Preconditions;


public class RestServiceImpl implements RestService
{

    private final HostRegistry hostRegistry;


    public RestServiceImpl( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    @Override
    public Response getHosts()
    {
        Set<ResourceHostInfo> hosts = hostRegistry.getResourceHostsInfo();

        return Response.ok( JsonUtil.toJson( hosts ) ).build();
    }
}
