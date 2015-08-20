package io.subutai.core.security.broker;


import java.io.InputStream;
import java.util.UUID;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.security.crypto.pgp.PgpUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.broker.api.TextMessagePostProcessor;


/**
 * This class encrypts outgoing messages
 */
public class MessageEncryptor implements TextMessagePostProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageEncryptor.class.getName() );

    public static final String PUBLIC_KEYRING = "dummy.pkr";
    public static final String SECRET_KEYRING = "dummy.skr";

    public static final String PUBLIC_KEY_ID = "e2451337c277dbf1";
    public static final String SECRET_KEY_ID = "d558f9a4a0b450b3";

    public static final String SECRET_PWD = "12345678";


    @Override
    public String process( final String topic, final String message )
    {
        LOG.info( String.format( "OUTGOING:%s", message ) );

        try
        {
            //assume this is a host topic
            if ( UUIDUtil.isStringAUuid( topic ) )
            {

                //todo obtain MH private key
                PGPSecretKey peerKeyForSigning = PgpUtil.findSecretKeyById( findFile( SECRET_KEYRING ), SECRET_KEY_ID );
                //todo obtain target host pub key
                PGPPublicKey hostKeyForEncrypting =
                        PgpUtil.findPublicKeyById( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_ID );

                RequestWrapper requestWrapper = JsonUtil.fromJson( message, RequestWrapper.class );

                Request originalRequest = requestWrapper.getRequest();

                String encryptedRequestString = new String(
                        PgpUtil.signAndEncrypt( JsonUtil.toJson( originalRequest ).getBytes(), peerKeyForSigning,
                                SECRET_PWD, hostKeyForEncrypting, true ) );

                EncryptedRequestWrapper encryptedRequestWrapper =
                        new EncryptedRequestWrapper( encryptedRequestString, originalRequest.getId() );

                String encryptedRequestWrapperString = JsonUtil.toJson( encryptedRequestWrapper );

                LOG.info( String.format( "ENCRYPTED: %s", encryptedRequestWrapperString ) );

                return encryptedRequestWrapperString;
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in process", e );
        }

        return message;
    }


    public static InputStream findFile( final String file )
    {
        return MessageEncryptor.class.getClassLoader().getResourceAsStream( file );
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
