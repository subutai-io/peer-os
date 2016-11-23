package io.subutai.core.executor.rest;


import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.command.ResponseImpl;
import io.subutai.common.command.ResponseWrapper;
import io.subutai.common.host.HeartBeat;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.executor.api.RestProcessor;
import io.subutai.core.security.api.SecurityManager;


public class RestServiceImpl implements RestService
{
    private final static String INVALID_RESPONSE = "Invalid response";
    private final static String INVALID_HEARTBEAT = "Invalid heartbeat";
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private final SecurityManager securityManager;
    private final RestProcessor restProcessor;


    public RestServiceImpl( final SecurityManager securityManager, final RestProcessor restProcessor )
    {
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( restProcessor );

        this.securityManager = securityManager;
        this.restProcessor = restProcessor;
    }


    @RolesAllowed( "Resource-Management|Write" )
    @Override
    public Response processHeartbeat( final String heartbeat )
    {
        try
        {
            Preconditions.checkArgument( heartbeat != null && !heartbeat.trim().isEmpty(), INVALID_HEARTBEAT );

            String decryptedHeartbeat = decrypt( heartbeat );

            Preconditions.checkArgument( decryptedHeartbeat != null && !decryptedHeartbeat.trim().isEmpty(),
                    INVALID_HEARTBEAT );

            final HeartBeat heartBeat = JsonUtil.fromJson( decryptedHeartbeat, HeartBeat.class );

            Preconditions.checkNotNull( heartBeat, INVALID_HEARTBEAT );

            Preconditions.checkNotNull( heartBeat.getHostInfo(), INVALID_HEARTBEAT );

            restProcessor.handleHeartbeat( heartBeat );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing heartbeat from agent: {}", e.getMessage() );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @RolesAllowed( "Resource-Management|Write" )
    @Override
    public Response processResponse( final String response )
    {
        try
        {
            Preconditions.checkArgument( response != null && !response.trim().isEmpty(), INVALID_RESPONSE );

            String decryptedResponse = decrypt( response );

            Preconditions.checkArgument( decryptedResponse != null && !decryptedResponse.trim().isEmpty(),
                    INVALID_RESPONSE );

            ResponseWrapper responseWrapper = JsonUtil.fromJson( decryptedResponse, ResponseWrapper.class );

            Preconditions.checkNotNull( responseWrapper, INVALID_RESPONSE );

            Preconditions.checkNotNull( responseWrapper.getResponse(), INVALID_RESPONSE );

            final ResponseImpl responseImpl = responseWrapper.getResponse();

            restProcessor.handleResponse( responseImpl );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error processing command response from agent: {}", e.getMessage() );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @RolesAllowed( "Resource-Management|Read" )
    @Override
    public Response getRequests( String hostId )
    {
        try
        {
            Set<String> hostRequests = restProcessor.getRequests( hostId );

            if ( CollectionUtil.isCollectionEmpty( hostRequests ) )
            {
                LOG.debug( String.format( "Requested commands for RH %s. No requests", hostId ) );

                return Response.noContent().build();
            }
            else
            {
                LOG.debug( String.format( "Requested commands for RH %s. %d requests", hostId, hostRequests.size() ) );

                return Response.ok( hostRequests.toString() ).build();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error feeding commands to agent", e );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @RolesAllowed( "Resource-Management|Read" )
    @Override
    public Response check( final String hostId )
    {
        if ( securityManager.getKeyManager().getPublicKey( hostId ) != null )
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    private String decrypt( String message ) throws PGPException
    {
        return securityManager.decryptNVerifyResponseFromHost( message );
    }
}
