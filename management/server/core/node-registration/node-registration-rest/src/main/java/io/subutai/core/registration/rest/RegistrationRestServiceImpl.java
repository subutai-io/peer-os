package io.subutai.core.registration.rest;


import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.rest.transitional.HostRequest;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


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
        return Response.ok( securityManager.getKeyManager().getPeerPublicKeyring() ).build();
    }


    @Override
    public Response registerPublicKey( final String message )
    {
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();
        KeyManager keyManager = securityManager.getKeyManager();
        InputStream secretKey = PGPEncryptionUtil.getFileInputStream( keyManager.getSecretKeyringFile() );

        byte[] decrypted = encryptionTool.decrypt( message.getBytes(), secretKey, keyManager.getSecretKeyringPwd() );
        try
        {
            String decryptedMessage = new String( decrypted, "UTF-8" );
            RequestedHost temp = JsonUtil.fromJson( decryptedMessage, HostRequest.class );

            Message interceptor = PhaseInterceptorChain.getCurrentMessage();
            HttpServletRequest request = ( HttpServletRequest ) interceptor.get( AbstractHTTPDestination.HTTP_REQUEST );
            temp.setRestHook( String.format( "%s:%s", request.getRemoteAddr(), temp.getRestHook() ) );

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
        KeyManager keyManager = securityManager.getKeyManager();
        InputStream secretKey = PGPEncryptionUtil.getFileInputStream( keyManager.getSecretKeyringFile() );

        byte[] decrypted = encryptionTool.decrypt( message.getBytes(), secretKey, keyManager.getSecretKeyringPwd() );
        try
        {
            String decryptedMessage = new String( decrypted, "UTF-8" );

            String token =
                    decryptedMessage.substring( 0, decryptedMessage.indexOf( System.getProperty( "line.separator" ) ) );
            String publicKey = decryptedMessage
                    .substring( decryptedMessage.indexOf( System.getProperty( "line.separator" ) ) + 1 );
            registrationManager.verifyToken( token, publicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error decrypting file.", e );
            return Response.serverError().build();
        }

        return Response.ok( "Accepted" ).build();
    }
}
