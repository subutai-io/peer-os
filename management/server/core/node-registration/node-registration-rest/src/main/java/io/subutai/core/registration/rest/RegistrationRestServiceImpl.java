package io.subutai.core.registration.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.rest.transitional.RequestedHostJson;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * Created by talas on 8/25/15.
 */
public class RegistrationRestServiceImpl implements RegistrationRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RegistrationRestServiceImpl.class );
    private SecurityManager securityManager;
    private RegistrationManager registrationManager;


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


    @Override
    public Response registerPublicKey( final String message )
    {
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();

        try
        {
            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, "UTF-8" );
            RequestedHost temp = JsonUtil.fromJson( decryptedMessage, RequestedHostJson.class );

            registrationManager.queueRequest( temp );

        }
        catch ( Exception e )
        {
            LOGGER.error( "Error decrypting file.", e );
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
            LOGGER.error( "Error decrypting file.", e );
            return Response.serverError().build();
        }

        return Response.ok( "Accepted" ).build();
    }
}
