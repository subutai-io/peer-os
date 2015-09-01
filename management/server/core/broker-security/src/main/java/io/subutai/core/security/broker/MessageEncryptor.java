package io.subutai.core.security.broker;


import java.util.UUID;

import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.broker.api.TextMessagePostProcessor;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * This class encrypts outgoing messages
 */
public class MessageEncryptor implements TextMessagePostProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageEncryptor.class.getName() );

    private final boolean encryptionEnabled;


    public MessageEncryptor( final boolean encryptionEnabled )
    {
        this.encryptionEnabled = encryptionEnabled;
    }


    public static SecurityManager getSecurityManager() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( SecurityManager.class );
    }


    @Override
    public String process( final String topic, final String message )
    {
        //assume this is a host  topic
        if ( encryptionEnabled && UUIDUtil.isStringAUuid( topic ) )
        {
            try
            {
                EncryptionTool encryptionTool = getSecurityManager().getEncryptionTool();

                //obtain target host pub key for encrypting
                PGPPublicKey hostKeyForEncrypting =
                        MessageEncryptor.getSecurityManager().getKeyManager().getPublicKey( topic );

                RequestWrapper requestWrapper = JsonUtil.fromJson( message, RequestWrapper.class );

                Request originalRequest = requestWrapper.getRequest();

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


    class RequestWrapper
    {
        private RequestBuilder.RequestImpl request;


        public RequestBuilder.RequestImpl getRequest()
        {
            return request;
        }
    }


    class EncryptedRequestWrapper
    {
        private final String request;
        private final UUID hostId;


        public EncryptedRequestWrapper( final String request, final UUID hostId )
        {
            this.request = request;
            this.hostId = hostId;
        }
    }
}
