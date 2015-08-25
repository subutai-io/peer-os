package io.subutai.core.registration.rest;


import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.util.JsonUtil;
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


    public RegistrationRestServiceImpl( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    @Override
    public Response getPublicKey()
    {
        Map<String, String> result = Maps.newHashMap();
        result.put( "Key", securityManager.getKeyManager().getPeerPublicKeyring() );
        return Response.ok( JsonUtil.toJson( result ) ).build();
    }


    @Override
    public Response registerPublicKey( final String message )
    {
        LOGGER.error( message );
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();
        KeyManager keyManager = securityManager.getKeyManager();
        InputStream secretKey = PGPEncryptionUtil.getFileInputStream( keyManager.getSecretKeyringFile() );

        byte[] decrypted = encryptionTool.decrypt( message.getBytes(), secretKey, keyManager.getSecretKeyringPwd() );
        try
        {
            String decryptedMessage = new String( decrypted, "UTF-8" );
            LOGGER.error( decryptedMessage );
            Map<String, String> requestedHost =
                    JsonUtil.fromJson( decryptedMessage, new TypeToken<Map<String, String>>()
                    {
                    }.getType() );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error decrypting file.", e );
        }

        return Response.ok().build();
    }
}
