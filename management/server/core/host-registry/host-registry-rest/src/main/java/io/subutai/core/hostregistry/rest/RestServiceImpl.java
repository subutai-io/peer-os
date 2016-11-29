package io.subutai.core.hostregistry.rest;


import java.util.Set;

import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.hostregistry.api.HostRegistry;


public class RestServiceImpl implements RestService
{

    private final HostRegistry hostRegistry;


    public RestServiceImpl( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    //TODO check if used, and set appropriate permission
    @Override
    public Response getHosts()
    {
        Set<ResourceHostInfo> hosts = hostRegistry.getResourceHostsInfo();

        return Response.ok( JsonUtil.toJson( hosts ) ).build();
    }
}
