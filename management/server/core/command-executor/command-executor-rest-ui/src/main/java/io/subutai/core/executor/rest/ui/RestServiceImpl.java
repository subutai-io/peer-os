package io.subutai.core.executor.rest.ui;


import javax.ws.rs.core.Response;

import io.subutai.common.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.common.command.RequestBuilder;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private CommandExecutor commandExecutor;


    public RestServiceImpl( final CommandExecutor commandExecutor )
    {
        Preconditions.checkNotNull( commandExecutor );

        this.commandExecutor = commandExecutor;
    }

    @Override
    public Response executeCommand( final String hostId, final String command, final String path )
    {
        try {
            Preconditions.checkNotNull( hostId, "Invalid host id" );
            Preconditions.checkNotNull( command, "Invalid command" );

            RequestBuilder request = new RequestBuilder( command );
            request.withCwd( path );
            CommandResult result = commandExecutor.execute( hostId, request );

            return Response.ok().entity( JsonUtil.toJson( result )).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error on execute command #executeCommand", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity(e.toString()).build();
        }
    }
}