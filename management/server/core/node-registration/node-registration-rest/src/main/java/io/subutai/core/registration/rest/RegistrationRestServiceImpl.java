package io.subutai.core.registration.rest;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.resource.host.RequestedHost;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


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

        try
        {
            byte[] decrypted = encryptionTool.decrypt( message.getBytes() );
            String decryptedMessage = new String( decrypted, "UTF-8" );
            RequestedHost temp = JsonUtil.fromJson( decryptedMessage, HostRequest.class );

            Message interceptor = PhaseInterceptorChain.getCurrentMessage();
            HttpServletRequest request = ( HttpServletRequest ) interceptor.get( AbstractHTTPDestination.HTTP_REQUEST );
            temp.setRestHook( String.format( "%s:%s", request.getRemoteAddr(), temp.getRestHook() ) );

            registrationManager.queueRequest( temp );
            keyManager.savePublicKeyRing( temp.getId(), temp.getPublicKey() );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error decrypting file.", e );
        }

        return Response.ok().build();
    }
}
