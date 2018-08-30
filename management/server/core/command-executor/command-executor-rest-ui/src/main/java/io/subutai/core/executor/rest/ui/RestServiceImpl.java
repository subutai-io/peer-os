package io.subutai.core.executor.rest.ui;


import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.api.IdentityManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private static final long RESULT_TTL_SEC = 3600; //1 hr
    private EnvironmentManager environmentManager;
    private IdentityManager identityManager;
    private LocalPeer localPeer;
    private ExpiringCache<String, CommandResult> resultCache = new ExpiringCache<>();


    public RestServiceImpl( final EnvironmentManager environmentManager, final IdentityManager identityManager,
                            final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( localPeer );

        this.environmentManager = environmentManager;
        this.identityManager = identityManager;
        this.localPeer = localPeer;
    }


    @RolesAllowed( { "Environment-Management|Write", "Environment-Management|Update" } )
    @Override
    public Response executeCommandAsync( final String hostId, final String command, String environmentId,
                                         final String path, final Boolean daemon, final Integer timeOut )
    {
        try
        {
            Preconditions.checkArgument( !StringUtils.isBlank( hostId ), "Invalid host id" );
            Preconditions.checkArgument( !StringUtils.isBlank( command ), "Invalid command" );

            RequestBuilder commandReq = createCommand( command, path, daemon, timeOut );

            Host host = getHost( environmentId, hostId );

            if ( host != null )
            {
                final String id = UUID.randomUUID().toString();

                final long resultTTL = TimeUnit.SECONDS.toMillis( commandReq.getTimeout() + RESULT_TTL_SEC );

                //prepopulate with empty result
                resultCache.put( id, new CommandResultImpl( null, "", "", CommandStatus.NEW ), resultTTL );

                host.executeAsync( commandReq, new CommandCallback()
                {
                    @Override
                    public void onResponse( final io.subutai.common.command.Response response,
                                            final CommandResult commandResult )
                    {
                        resultCache.put( id, commandResult, resultTTL );
                    }
                } );

                return Response.ok().entity( JsonUtil.toJson( "id", id ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND )
                               .entity( JsonUtil.toJson( "ERROR", "Host not found" ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in #executeCommandAsync", e );

            return Response.serverError().entity( JsonUtil.toJson( "ERROR", e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Environment-Management|Read" } )
    @Override
    public Response getCommandResult( final String id )
    {
        try
        {
            Preconditions.checkArgument( !StringUtils.isBlank( id ), "Invalid id" );

            CommandResult result = resultCache.get( id.trim() );

            if ( result != null )
            {
                return Response.ok().entity( JsonUtil.toJson( result ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND )
                               .entity( JsonUtil.toJson( "ERROR", "Command not found" ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in #getCommandResult", e );

            return Response.serverError().entity( JsonUtil.toJson( "ERROR", e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Environment-Management|Write", "Environment-Management|Update" } )
    @Override
    public Response executeCommand( final String hostId, final String command, String environmentId, final String path,
                                    final Boolean daemon, final Integer timeOut )
    {
        try
        {
            Preconditions.checkArgument( !StringUtils.isBlank( hostId ), "Invalid host id" );
            Preconditions.checkArgument( !StringUtils.isBlank( command ), "Invalid command" );

            RequestBuilder commandReq = createCommand( command, path, daemon, timeOut );

            Host host = getHost( environmentId, hostId );

            if ( host != null )
            {
                return Response.ok().entity( JsonUtil.toJson( host.execute( commandReq ) ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND )
                               .entity( JsonUtil.toJson( "ERROR", "Host not found" ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in #executeCommand", e );

            return Response.serverError().entity( JsonUtil.toJson( "ERROR", e.getMessage() ) ).build();
        }
    }


    private RequestBuilder createCommand( final String command, final String path, final Boolean daemon,
                                          final Integer timeOut )
    {
        RequestBuilder request = new RequestBuilder( command );

        if ( !StringUtils.isBlank( path ) )
        {
            request.withCwd( path );
        }

        if ( daemon != null && daemon )
        {
            request.daemon();
        }

        if ( timeOut != null && timeOut > 0 )
        {
            request.withTimeout( timeOut );
        }

        return request;
    }


    private Host getHost( String environmentId, String hostId )
    {
        Host host;

        //search in the specified environment
        if ( environmentId != null )
        {
            try
            {
                return environmentManager.loadEnvironment( environmentId ).getContainerHostById( hostId );
            }
            catch ( EnvironmentNotFoundException | ContainerHostNotFoundException e )
            {
                return null;
            }
        }
        else
        {
            // check if this command is intended to run on RH
            if ( identityManager.isAdmin() )
            {
                try
                {
                    host = localPeer.getResourceHostById( hostId );
                }
                catch ( HostNotFoundException e )
                {
                    // try to find a container
                    host = getEnvironmentContainerById( hostId );
                }
            }
            // try to find a container
            else
            {
                host = getEnvironmentContainerById( hostId );
            }
        }

        return host;
    }


    private ContainerHost getEnvironmentContainerById( String containerId )
    {
        for ( final Environment environment : environmentManager.getEnvironments() )
        {
            try
            {
                return environment.getContainerHostById( containerId );
            }
            catch ( ContainerHostNotFoundException ex )
            {
                //ignore
            }
        }

        return null;
    }
}