package io.subutai.common.pgp.message;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

import io.subutai.common.pgp.crypto.PGPDecrypt;
import io.subutai.common.pgp.crypto.PGPEncrypt;
import io.subutai.common.pgp.crypto.PGPSign;
import io.subutai.common.pgp.crypto.PGPVerify;


/**
 * Helps with signing and encryption a message between the sender and the recipient.
 */
public class PGPMessenger
{
    private final PGPPrivateKey SENDER_PRIVATE_KEY;

    private final PGPPublicKey RECIPIENT_PUBLIC_KEY;


    public PGPMessenger( PGPPrivateKey senderPrivateKey, PGPPublicKey recipientPublicKey )
    {
        SENDER_PRIVATE_KEY = senderPrivateKey;

        RECIPIENT_PUBLIC_KEY = recipientPublicKey;
    }


    /**
     * Send the data from the sender to the recipient: - signing with the sender's private key - encrypt with the
     * recipient's public key
     */
    public byte[] produce( byte[] data ) throws PGPException
    {
        try
        {
            byte[] signedData = PGPSign.sign( data, SENDER_PRIVATE_KEY );

            return PGPEncrypt.encrypt( signedData, RECIPIENT_PUBLIC_KEY );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Cannot sign and encrypt a message.", e );
        }
    }


    /**
     * Get the data from the recipient to the sender: - decrypt with the sender's private key - verify the signature
     * with the recipient's public key
     */
    public byte[] consume( byte[] encData ) throws PGPException
    {
        try
        {
            byte[] signedData = PGPDecrypt.decrypt( encData, SENDER_PRIVATE_KEY );

            return PGPVerify.verify( signedData, RECIPIENT_PUBLIC_KEY );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Cannot decrypt and verify a signature.", e );
        }
    }
}