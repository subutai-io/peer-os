package io.subutai.core.registration.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.rest.transitional.RequestedHostJson;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


public class RegistrationRestServiceImpl implements RegistrationRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RegistrationRestServiceImpl.class );
    private SecurityManager securityManager;
    private RegistrationManager registrationManager;
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();


    public RegistrationRestServiceImpl( final SecurityManager securityManager,
                                        final RegistrationManager registrationManager )
    {
        Preconditions.checkNotNull( securityManager );
        Preconditions.checkNotNull( registrationManager );

        this.securityManager = securityManager;
        this.registrationManager = registrationManager;
    }


    @Override
    public Response registerPublicKey( final String message )
    {
        try
        {
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, "UTF-8" );
            RequestedHost requestedHost = JsonUtil.fromJson( decryptedMessage, RequestedHostJson.class );

            registrationManager.queueRequest( requestedHost );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error registering public key", e );
            return Response.serverError().build();
        }
    }


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
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response unRegisterRequest( final String requestId )
    {
        try
        {
            registrationManager.rejectRequest( requestId );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


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
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response verifyContainerToken( final String message )
    {
        try
        {
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, "UTF-8" );
            String lineSeparator = System.getProperty( "line.separator" );

            String token = decryptedMessage.substring( 0, decryptedMessage.indexOf( lineSeparator ) );
            decryptedMessage = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            String containerId = decryptedMessage.substring( 0, decryptedMessage.indexOf( lineSeparator ) );

            String publicKey = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            registrationManager.verifyToken( token, containerId, publicKey );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error verifying container token", e );
            return Response.serverError().build();
        }
    }


    @Override
    public Response getRegistrationRequests()
    {
        String result = gson.toJson( registrationManager.getRequests() );
        return Response.ok( result ).build();
    }
}
