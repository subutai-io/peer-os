package io.subutai.core.registration.rest;


import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.rest.transitional.RequestedHostJson;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


public class RegistrationRestServiceImpl implements RegistrationRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RegistrationRestServiceImpl.class );
    private static final String ERROR_KEY = "ERROR";
    private SecurityManager securityManager;
    private HostRegistrationManager registrationManager;
    private LocalPeer localPeer;


    public RegistrationRestServiceImpl( final SecurityManager securityManager,
                                        final HostRegistrationManager registrationManager, final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( registrationManager );
        Preconditions.checkNotNull( localPeer );

        this.securityManager = securityManager;
        this.registrationManager = registrationManager;
        this.localPeer = localPeer;
    }


    @RolesAllowed( "Resource-Management|Write" )
    @Override
    public Response registerPublicKey( final String message )
    {
        try
        {
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, StandardCharsets.UTF_8 );
            RequestedHost requestedHost = JsonUtil.fromJson( decryptedMessage, RequestedHostJson.class );

            registrationManager.queueRequest( requestedHost );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error registering public key: {}", e.getMessage() );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Write", "Resource-Management|Update" } )
    @Override
    public Response changeRhHostname( final String rhId, final String hostname )
    {
        try
        {
            registrationManager.changeRhHostname( rhId, hostname );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error changing RH hostname", e );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Write", "Resource-Management|Update" } )
    @Override
    public Response approveRegistrationRequest( final String requestId )
    {
        try
        {
            registrationManager.approveRequest( requestId );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving registration request", e );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Delete", "Resource-Management|Update" } )
    @Override
    public Response rejectRequest( final String requestId )
    {
        try
        {
            registrationManager.rejectRequest( requestId );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error rejecting registration request", e );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Delete", "Resource-Management|Update" } )
    @Override
    public Response removeRequest( final String requestId )
    {
        try
        {
            registrationManager.removeRequest( requestId );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error removing registration request", e );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Delete", "Resource-Management|Update" } )
    @Override
    public Response unblockRequest( final String requestId )
    {
        try
        {
            registrationManager.unblockRequest( requestId );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error unblocking registration request", e );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Write", "Resource-Management|Update" } )
    @Override
    public Response verifyContainerToken( final String message )
    {
        try
        {
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, StandardCharsets.UTF_8 );
            String lineSeparator = System.getProperty( "line.separator" );

            String token = decryptedMessage.substring( 0, decryptedMessage.indexOf( lineSeparator ) );
            decryptedMessage = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            String containerId = decryptedMessage.substring( 0, decryptedMessage.indexOf( lineSeparator ) );

            String publicKey = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            boolean valid = registrationManager.verifyTokenAndRegisterKey( token, containerId, publicKey );

            return valid ? Response.accepted().build() :
                   Response.status( Response.Status.UNAUTHORIZED ).entity( "Invalid token" ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error verifying container token", e );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @RolesAllowed( { "Resource-Management|Read" } )
    @Override
    public Response getRegistrationRequests()
    {
        try
        {
            List<RequestedHost> requestedHosts = registrationManager.getRequests();

            String managementId = null;
            try
            {
                ResourceHost managementHost = localPeer.getManagementHost();

                managementId = managementHost.getId();
            }
            catch ( HostNotFoundException ignore )
            {
            }

            List<RequestedHostJson> requestedHostList = Lists.newArrayList();

            for ( RequestedHost requestedHost : requestedHosts )
            {
                RequestedHostJson requestedHostJson = new RequestedHostJson( requestedHost );

                if ( requestedHost.getId().equalsIgnoreCase( managementId ) )
                {
                    requestedHostJson.setManagement( true );
                }

                try
                {
                    ResourceHost resourceHost = localPeer.getResourceHostById( requestedHost.getId() );

                    boolean connected = resourceHost.isConnected();

                    requestedHostJson.setConnected( connected );

                    requestedHostJson.setIp( resourceHost.getAddress() );

                    if ( resourceHost.ping() )
                    {
                        requestedHostJson.setVersion( resourceHost.getRhVersion() );
                    }
                }
                catch ( HostNotFoundException | ResourceHostException e )
                {
                    //ignore
                }

                requestedHostList.add( requestedHostJson );
            }

            String result = JsonUtil.toJson( requestedHostList );

            return Response.ok( result ).build();
        }
        catch ( Exception e )
        {
            LOGGER.warn( "Error in getRegistrationRequests {}", e.getMessage() );

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }
}
