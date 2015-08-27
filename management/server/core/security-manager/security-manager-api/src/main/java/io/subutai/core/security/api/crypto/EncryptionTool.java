package io.subutai.core.security.api.crypto;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import io.subutai.common.security.crypto.pgp.ContentAndSignatures;


/**
 * Tool for working with encryption
 */
public interface EncryptionTool
{

    /**
     * *****************************************
     */
    public byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored );


    /**
     * Decrypts message with Peer private key
     */
    public byte[] decrypt( final byte[] message ) throws PGPException;


    /**
     * *****************************************
     */
    public boolean verify( byte[] signedMessage, PGPPublicKey publicKey );


    /**
     * Signs message with peer private key and encrypts with the given pub key
     *
     * @param message - message
     * @param publicKey - encryption key
     * @param armored - output in armored format
     */
    public byte[] signAndEncrypt( final byte[] message, final PGPPublicKey publicKey, final boolean armored )
            throws PGPException;


    /**
     * Decrypts message with peer private key
     *
     * @param encryptedMessage - message
     *
     * @return - {@code ContentAndSignatures}
     */
    public ContentAndSignatures decryptAndReturnSignatures( final byte[] encryptedMessage ) throws PGPException;

    /**
     * Verifies the content with its signatures
     *
     * @param contentAndSignatures -  {@code ContentAndSignatures}
     * @param publicKey - public key to verify signatures
     *
     * @return - true if verified successfully, false otherwise
     */

    public boolean verifySignature( ContentAndSignatures contentAndSignatures, PGPPublicKey publicKey )
            throws PGPException;
}
