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


    @Override
    public byte[] process( final String topic, final byte[] message )
    {
        LOG.info( String.format( "INCOMING:%s", new String( message ) ) );

        try
        {
            //process incoming heartbeats and responses
            if ( Topic.RESPONSE_TOPIC.name().equalsIgnoreCase( topic ) || Topic.HEARTBEAT_TOPIC.name().equalsIgnoreCase(
                    topic ) )
            {
                //imitate obtaining MH private key and sender host pub key
                PGPSecretKey signingKey = PGPEncryptionUtil
                        .findSecretKeyById( MessageEncryptor.findFile( MessageEncryptor.SECRET_KEYRING ),
                                MessageEncryptor.SECRET_KEY_ID );
                PGPPublicKey encryptingKey = PGPEncryptionUtil
                        .findPublicKeyById( MessageEncryptor.findFile( MessageEncryptor.PUBLIC_KEYRING ),
                                MessageEncryptor.PUBLIC_KEY_ID );

                //todo obtain MH private key
                PGPSecretKey peerKeyForDecrypting = PGPEncryptionUtil
                        .findSecretKeyByFingerprint( MessageEncryptor.findFile( MessageEncryptor.SECRET_KEYRING ),
                                PGPEncryptionUtil.BytesToHex( encryptingKey.getFingerprint() ) );

                PGPEncryptionUtil.ContentAndSignatures contentAndSignatures = PGPEncryptionUtil
                        .decryptAndReturnSignatures( message, peerKeyForDecrypting, MessageEncryptor.SECRET_PWD );

                //todo obtain target host pub key by id from content
                PGPPublicKey hostKeyForVerifying = signingKey.getPublicKey();

                if ( PGPEncryptionUtil.verifySignature( contentAndSignatures, hostKeyForVerifying ) )
                {
                    LOG.info( String.format( "Verification succeeded%nDecrypted Message: %s",
                            new String( contentAndSignatures.getDecryptedContent() ) ) );
                }
                else
                {
                    LOG.info( String.format( "Verification failed%nDecrypted Message: %s",
                            new String( contentAndSignatures.getDecryptedContent() ) ) );
                }

                return contentAndSignatures.getDecryptedContent();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in process", e );
        }

        return message;
    }
}
