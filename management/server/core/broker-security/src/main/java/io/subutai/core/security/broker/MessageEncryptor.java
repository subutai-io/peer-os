package io.subutai.core.security.broker;


import java.util.UUID;

import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.broker.api.TextMessagePostProcessor;
import io.subutai.core.security.api.SecurityManager;


/**
 * This class encrypts outgoing messages
 */
public class MessageEncryptor implements TextMessagePostProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageEncryptor.class.getName() );

    public static final String SECRET_PWD = "12345678";

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
        LOG.info( String.format( "OUTGOING:%s", message ) );

        //assume this is a host  topic
        if ( encryptionEnabled && UUIDUtil.isStringAUuid( topic ) )
        {
            try
            {
                //obtain peer private key for signing
                PGPSecretKey peerKeyForSigning = getSecurityManager().getKeyManager().getSecretKey( null );

                //todo obtain target host pub key for encrypting
                //until then imitate obtaining target host pub key
                PGPPublicKey hostKeyForEncrypting = peerKeyForSigning.getPublicKey();

                RequestWrapper requestWrapper = JsonUtil.fromJson( message, RequestWrapper.class );

                Request originalRequest = requestWrapper.getRequest();

                String encryptedRequestString = new String( PGPEncryptionUtil
                        .signAndEncrypt( JsonUtil.toJson( originalRequest ).getBytes(), peerKeyForSigning, SECRET_PWD,
                                hostKeyForEncrypting, true ) );

                EncryptedRequestWrapper encryptedRequestWrapper =
                        new EncryptedRequestWrapper( encryptedRequestString, originalRequest.getId() );

                String encryptedRequestWrapperString = JsonUtil.toJson( encryptedRequestWrapper );

                LOG.info( String.format( "Sending encrypted message: %s", encryptedRequestWrapperString ) );

                return encryptedRequestWrapperString;
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
