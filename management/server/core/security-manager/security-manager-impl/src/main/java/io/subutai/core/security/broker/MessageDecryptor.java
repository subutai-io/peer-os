package io.subutai.core.security.broker;


import java.util.UUID;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.broker.api.ByteMessagePreProcessor;
import io.subutai.core.broker.api.Topic;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * This class decrypts incoming messages
 */
public class MessageDecryptor implements ByteMessagePreProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageDecryptor.class.getName() );

    private final boolean encryptionEnabled;


    public MessageDecryptor( final boolean encryptionEnabled )
    {
        this.encryptionEnabled = encryptionEnabled;
    }


    @Override
    public byte[] process( final String topic, final byte[] message )
    {
        LOG.debug( String.format( "INCOMING %s", new String( message ) ) );

        //process incoming heartbeats and responses
        if ( encryptionEnabled && ( Topic.RESPONSE_TOPIC.name().equalsIgnoreCase( topic ) || Topic.HEARTBEAT_TOPIC
                .name().equalsIgnoreCase( topic ) ) )
        {
            try
            {

                EncryptionTool encryptionTool = MessageEncryptor.getSecurityManager().getEncryptionTool();

                ContentAndSignatures contentAndSignatures = encryptionTool.decryptAndReturnSignatures( message );

                //obtain target host pub key by id from content for verifying
                UUID hostId;

                if ( Topic.RESPONSE_TOPIC.name().equalsIgnoreCase( topic ) )
                {
                    //obtain host id from response
                    ResponseWrapper responseWrapper =
                            JsonUtil.fromJson( new String( contentAndSignatures.getDecryptedContent() ),
                                    ResponseWrapper.class );

                    hostId = responseWrapper.getResponse().getId();
                }
                else
                {
                    //obtain host id from heartbeat
                    HeartBeat heartBeat = JsonUtil.fromJson( new String( contentAndSignatures.getDecryptedContent() ),
                            HeartBeat.class );

                    hostId = heartBeat.getHostInfo().getId();
                }


                PGPPublicKey hostKeyForVerifying =
                        MessageEncryptor.getSecurityManager().getKeyManager().getPublicKey( hostId.toString() );

                if ( encryptionTool.verifySignature( contentAndSignatures, hostKeyForVerifying ) )
                {
                    return contentAndSignatures.getDecryptedContent();
                }
                else
                {
                    throw new IllegalArgumentException( String.format( "Verification failed%nDecrypted Message: %s",
                            new String( contentAndSignatures.getDecryptedContent() ) ) );
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error in process", e );
            }
        }

        return message;
    }
}
