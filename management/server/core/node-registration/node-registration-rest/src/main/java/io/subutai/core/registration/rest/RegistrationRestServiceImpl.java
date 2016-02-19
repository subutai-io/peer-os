package io.subutai.core.registration.rest;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.subutai.common.settings.Common;
import io.subutai.common.util.IPUtil;
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
        this.securityManager = securityManager;
        this.registrationManager = registrationManager;
    }


    @Override
    public Response getPublicKey()
    {
        return Response.ok( securityManager.getKeyManager().getPublicKeyRingAsASCII( null ) ).build();
    }


    protected String getClientIP()
    {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
        return request.getRemoteAddr();
    }


    @Override
    public Response registerPublicKey( final String message )
    {
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();

        try
        {
            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, "UTF-8" );
            RequestedHost requestedHost = JsonUtil.fromJson( decryptedMessage, RequestedHostJson.class );

            registrationManager.queueRequest( requestedHost );

            //temp workaround to approve MH automatically
            if ( IPUtil.getLocalIps().contains( getClientIP() ) && Common.MANAGEMENT_HOSTNAME
                    .equalsIgnoreCase( requestedHost.getHostname() ) )
            {
                registrationManager.approveRequest( requestedHost.getId() );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error registering public key", e );
            return Response.serverError().build();
        }

        return Response.ok().build();
    }


    @Override
    public Response verifyContainerToken( final String message )
    {
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();

        try
        {
            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, "UTF-8" );
            String lineSeparator = System.getProperty( "line.separator" );

            String token = decryptedMessage.substring( 0, decryptedMessage.indexOf( lineSeparator ) );
            decryptedMessage = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            String containerId = decryptedMessage.substring( 0, decryptedMessage.indexOf( lineSeparator ) );
            //decryptedMessage = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            String publicKey = decryptedMessage.substring( decryptedMessage.indexOf( lineSeparator ) + 1 );

            registrationManager.verifyToken( token, containerId, publicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error verifying container token", e );
            return Response.serverError().build();
        }

        return Response.ok( "Accepted" ).build();
    }


    @Override
    public Response getRegistrationRequests()
    {
        String result = gson.toJson( registrationManager.getRequests() );
        return Response.ok( result ).build();
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
            return Response.serverError().build();
        }
    }
}
