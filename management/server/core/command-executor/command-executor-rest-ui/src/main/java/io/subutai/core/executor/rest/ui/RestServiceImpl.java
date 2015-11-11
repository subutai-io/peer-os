package io.subutai.core.executor.rest.ui;


import java.util.Set;

import javax.ws.rs.core.Response;

import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.common.command.RequestBuilder;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private CommandExecutor commandExecutor;
    private HostRegistry hostRegistry;
    private EnvironmentManager environmentManager;


    public RestServiceImpl( final CommandExecutor commandExecutor, final HostRegistry hostRegistry, final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( commandExecutor );
        Preconditions.checkNotNull( hostRegistry );

        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.environmentManager = environmentManager;
    }

    @Override
    public Response executeCommand( final String hostId, final String content )
    {
        try {
            Preconditions.checkNotNull( hostId, "Invalid host id" );

            RequestBuilder command = new RequestBuilder( content );
            commandExecutor.executeAsync( hostId, command );
            return Response.ok().entity( JsonUtil.toJson( "ok" ) ).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error on execute command #executeCommand", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity(e.toString()).build();
        }
    }


    @Override
    public Response getResourceHosts()
    {
        Set<ResourceHostInfo> reply = hostRegistry.getResourceHostsInfo();
        return Response.ok().entity( JsonUtil.toJson( hostRegistry.getResourceHostsInfo() ) ).build();
    }
}