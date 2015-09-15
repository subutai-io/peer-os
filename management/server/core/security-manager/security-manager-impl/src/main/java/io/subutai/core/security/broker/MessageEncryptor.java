package io.subutai.core.security.broker;


import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.broker.api.TextMessagePostProcessor;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * This class encrypts outgoing messages
 */
public class MessageEncryptor implements TextMessagePostProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageEncryptor.class.getName() );
    private static final String CLONE_CMD_REGEX = "\\s*subutai\\s*clone[\\s\\S]*?";

    private final boolean encryptionEnabled;


    public MessageEncryptor( final boolean encryptionEnabled )
    {
        this.encryptionEnabled = encryptionEnabled;
    }


    public static SecurityManager getSecurityManager() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( SecurityManager.class );
    }


    public static RegistrationManager getRegistrationManager() throws NamingException
    {

        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    @Override
    public String process( final String topic, final String message )
    {
        LOG.debug( String.format( "OUTGOING %s", message ) );

        //assume this is a host  topic
        if ( encryptionEnabled )
        {
            try
            {
                EncryptionTool encryptionTool = getSecurityManager().getEncryptionTool();

                //obtain target host pub key for encrypting
                PGPPublicKey hostKeyForEncrypting =
                        MessageEncryptor.getSecurityManager().getKeyManager().getPublicKey( topic );

                RequestWrapper requestWrapper = JsonUtil.fromJson( message, RequestWrapper.class );

                Request originalRequest = requestWrapper.getRequest();

                if ( originalRequest.getCommand().toLowerCase().matches( CLONE_CMD_REGEX ) )
                {
                    //add token for container creation
                    originalRequest =
                            new RequestBuilder.RequestImpl( originalRequest.getType(), originalRequest.getId(),
                                    originalRequest.getCommandId(), originalRequest.getWorkingDirectory(),
                                    String.format( "%s -t %s", originalRequest.getCommand(), getRegistrationManager()
                                            .generateContainerTTLToken( ( originalRequest.getTimeout()
                                                    + Common.WAIT_CONTAINER_CONNECTION_SEC + 10 ) * 1000L )
                                            .getToken() ), originalRequest.getArgs(), originalRequest.getEnvironment(),
                                    originalRequest.getStdOut(), originalRequest.getStdErr(),
                                    originalRequest.getRunAs(), originalRequest.getTimeout(),
                                    originalRequest.isDaemon(), originalRequest.getConfigPoints(),
                                    originalRequest.getPid() );
                }

                String encryptedRequestString = new String( encryptionTool
                        .signAndEncrypt( JsonUtil.toJson( originalRequest ).getBytes(), hostKeyForEncrypting, true ) );

                EncryptedRequestWrapper encryptedRequestWrapper =
                        new EncryptedRequestWrapper( encryptedRequestString, originalRequest.getId() );

                return JsonUtil.toJson( encryptedRequestWrapper );
            }
            catch ( Exception e )
            {
                LOG.error( "Error in process", e );
            }
        }

        return message;
    }
}
