package io.subutai.core.security.broker;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.core.broker.api.ByteMessagePreProcessor;
import io.subutai.core.broker.api.Topic;


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
        LOG.info( String.format( "INCOMING:%s", new String( message ) ) );

        //process incoming heartbeats and responses
        if ( encryptionEnabled && ( Topic.RESPONSE_TOPIC.name().equalsIgnoreCase( topic ) || Topic.HEARTBEAT_TOPIC
                .name().equalsIgnoreCase( topic ) ) )
        {
            try
            {
                //obtain peer private key for decrypting
                PGPSecretKey peerKeyForDecrypting =
                        MessageEncryptor.getSecurityManager().getKeyManager().getSecretKey( null );

                PGPEncryptionUtil.ContentAndSignatures contentAndSignatures = PGPEncryptionUtil
                        .decryptAndReturnSignatures( message, peerKeyForDecrypting, MessageEncryptor.SECRET_PWD );

                //todo obtain target host pub key by id from content for verifying
                //until then imitate obtaining target host pub key
                PGPPublicKey hostKeyForVerifying = peerKeyForDecrypting.getPublicKey();

                if ( PGPEncryptionUtil.verifySignature( contentAndSignatures, hostKeyForVerifying ) )
                {
                    LOG.info( String.format( "Verification succeeded%nDecrypted Message: %s",
                            new String( contentAndSignatures.getDecryptedContent() ) ) );

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
