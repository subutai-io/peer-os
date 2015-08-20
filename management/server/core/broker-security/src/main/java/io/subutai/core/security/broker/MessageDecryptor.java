package io.subutai.core.security.broker;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PgpUtil;
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
                PGPSecretKey signingKey =
                        PgpUtil.findSecretKeyById( MessageEncryptor.findFile( MessageEncryptor.SECRET_KEYRING ),
                                MessageEncryptor.SECRET_KEY_ID );
                PGPPublicKey encryptingKey =
                        PgpUtil.findPublicKeyById( MessageEncryptor.findFile( MessageEncryptor.PUBLIC_KEYRING ),
                                MessageEncryptor.PUBLIC_KEY_ID );

                //todo obtain MH private key
                PGPSecretKey peerKeyForDecrypting = PgpUtil.findSecretKeyByFingerprint(
                        MessageEncryptor.findFile( MessageEncryptor.SECRET_KEYRING ),
                        PgpUtil.BytesToHex( encryptingKey.getFingerprint() ) );
                //todo obtain target host pub key
                PGPPublicKey hostKeyForVerifying = signingKey.getPublicKey();

                PgpUtil.ContentAndSignatures contentAndSignatures =
                        PgpUtil.decryptAndReturnSignatures( message, peerKeyForDecrypting,
                                MessageEncryptor.SECRET_PWD );

                if ( PgpUtil.verifySignature( contentAndSignatures, hostKeyForVerifying ) )
                {
                    LOG.info( String.format( "Verification succeeded%nDecrypted Message: %s",
                            new String( contentAndSignatures.getDecryptedContent() ) ) );
                }
                else
                {
                    LOG.info( String.format( "Verification failed%nDecrypted Message: %s",
                            new String( contentAndSignatures.getDecryptedContent() ) ) );
                }

                return PgpUtil.decryptAndVerify( message, peerKeyForDecrypting, MessageEncryptor.SECRET_PWD,
                        hostKeyForVerifying );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in process", e );
        }

        return message;
    }
}
