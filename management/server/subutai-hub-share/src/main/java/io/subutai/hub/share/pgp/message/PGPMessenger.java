package io.subutai.hub.share.pgp.message;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

import io.subutai.hub.share.pgp.crypto.PGPDecrypt;
import io.subutai.hub.share.pgp.crypto.PGPEncrypt;
import io.subutai.hub.share.pgp.crypto.PGPSign;
import io.subutai.hub.share.pgp.crypto.PGPVerify;


/**
 * Helps with signing and encryption a message between the sender and the recipient.
 *
 * Produce: send the data from the sender to the recipient:
 * - signing with the sender's private key
 * - encrypt with the recipient's public key
 *
 * Consume: get the data from the recipient to the sender:
 * - decrypt with the sender's private key
 * - verify the signature with the recipient's public key
 */
public class PGPMessenger
{
    private final PGPPrivateKey senderPrivateKey;

    private final PGPPublicKey recipientPublicKey;


    public PGPMessenger( PGPPrivateKey senderPrivateKey, PGPPublicKey recipientPublicKey )
    {
        this.senderPrivateKey = senderPrivateKey;

        this.recipientPublicKey = recipientPublicKey;
    }


    public byte[] produce( byte data[] ) throws PGPException
    {
        if ( data == null )
        {
            return null;
        }

        try
        {
            byte signedData[] = PGPSign.sign( data, senderPrivateKey );

            return PGPEncrypt.encrypt( signedData, recipientPublicKey );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Cannot sign and encrypt a message.", e );
        }
    }


    public byte[] consume( byte encData[] ) throws PGPException
    {
        if ( encData == null )
        {
            return null;
        }

        try
        {
            byte signedData[] = PGPDecrypt.decrypt( encData, senderPrivateKey );

            return PGPVerify.verify( signedData, recipientPublicKey );
        }
        catch ( Exception e )
        {
            throw new PGPException( "Cannot decrypt and verify a signature.", e );
        }
    }
}