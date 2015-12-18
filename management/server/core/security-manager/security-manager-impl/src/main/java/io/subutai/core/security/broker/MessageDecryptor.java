package io.subutai.core.security.broker;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.broker.api.ByteMessagePreProcessor;
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

        if ( encryptionEnabled )
        {
            try
            {

                EncryptionTool encryptionTool = MessageEncryptor.getSecurityManager().getEncryptionTool();

                EncryptedResponseWrapper responseWrapper =
                        JsonUtil.fromJson( new String( message ), EncryptedResponseWrapper.class );

                ContentAndSignatures contentAndSignatures =
                        encryptionTool.decryptAndReturnSignatures( responseWrapper.getResponse().getBytes() );

                PGPPublicKey hostKeyForVerifying = MessageEncryptor.getSecurityManager().getKeyManager()
                                                                   .getPublicKey( responseWrapper.getHostId() );

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
