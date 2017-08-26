package io.subutai.core.executor.rest.ui;


import java.security.AccessControlException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.identity.api.IdentityManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private CommandExecutor commandExecutor;
    private EnvironmentManager environmentManager;
    private IdentityManager identityManager;
    private LocalPeer localPeer;


    public RestServiceImpl( final CommandExecutor commandExecutor, final EnvironmentManager environmentManager,
                            final IdentityManager identityManager, final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( commandExecutor );
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( localPeer );

        this.commandExecutor = commandExecutor;
        this.environmentManager = environmentManager;
        this.identityManager = identityManager;
        this.localPeer = localPeer;
    }


    @RolesAllowed( { "Environment-Management|Write", "Environment-Management|Update" } )
    @Override
    public Response executeCommand( final String hostId, final String command, String environmentId, final String path,
                                    final Boolean daemon, final Integer timeOut )
    {
        try
        {
            Preconditions.checkNotNull( hostId, "Invalid host id" );
            Preconditions.checkNotNull( command, "Invalid command" );

            RequestBuilder request = new RequestBuilder( command );
            request.withCwd( path );

            if ( daemon != null && daemon )
            {
                request.daemon();
            }

            if ( timeOut != null && timeOut > 0 )
            {
                request.withTimeout( timeOut );
            }

            if ( environmentId == null )
            {
                for ( final Environment environment : environmentManager.getEnvironments() )
                {
                    try
                    {
                        environment.getContainerHostById( hostId );

                        environmentId = environment.getId();

                        break;
                    }
                    catch ( ContainerHostNotFoundException ex )
                    {
                        //ignore
                    }
                }
            }

            if ( environmentId != null )
            {
                try
                {
                    ContainerHost containerHost =
                            environmentManager.loadEnvironment( environmentId ).getContainerHostById( hostId );

                    return Response.ok().entity( JsonUtil.toJson( containerHost.execute( request ) ) ).build();
                }
                catch ( EnvironmentNotFoundException e )
                {
                    throw new AccessControlException( "Access denied" );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error executing command", e );

                    return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
                                   .entity( JsonUtil.toJson( e.getMessage() ) ).build();
                }
            }
            // this command is intended to run on RH
            else
            {
                if ( identityManager.isAdmin() )
                {
                    //check if host is RH otherwise throw access control exception
                    localPeer.getResourceHostById( hostId );

                    return Response.ok().entity( JsonUtil.toJson( commandExecutor.execute( hostId, request ) ) )
                                   .build();
                }
                else
                {
                    throw new AccessControlException( "Access denied" );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error on execute command #executeCommand", e );

            return Response.serverError().entity( JsonUtil.toJson( "ERROR", e.getMessage() ) ).build();
        }
    }
}