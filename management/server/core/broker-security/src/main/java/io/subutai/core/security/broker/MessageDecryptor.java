package io.subutai.core.security.broker;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PgpUtil;
import io.subutai.core.broker.api.ByteMessagePreProcessor;


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

        //imitate obtaining MH private key and sender host pub key
        try
        {
            PGPSecretKey signingKey =
                    PgpUtil.findSecretKeyById( MessageEncryptor.findFile( MessageEncryptor.SECRET_KEYRING ),
                            MessageEncryptor.SECRET_KEY_ID );
            PGPPublicKey encryptingKey =
                    PgpUtil.findPublicKeyById( MessageEncryptor.findFile( MessageEncryptor.PUBLIC_KEYRING ),
                            MessageEncryptor.PUBLIC_KEY_ID );

            PGPSecretKey decryptingSecretKey =
                    PgpUtil.findSecretKeyByFingerprint( MessageEncryptor.findFile( MessageEncryptor.SECRET_KEYRING ),
                            PgpUtil.BytesToHex( encryptingKey.getFingerprint() ) );


            return PgpUtil.decryptAndVerify( message, decryptingSecretKey, MessageEncryptor.SECRET_PWD,
                    signingKey.getPublicKey() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in process", e );
        }

        return message;
    }
}
