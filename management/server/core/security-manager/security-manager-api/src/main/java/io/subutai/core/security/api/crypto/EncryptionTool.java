package io.subutai.core.security.api.crypto;


import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

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
     * *****************************************
     */
    public byte[] decrypt( final byte[] message, final InputStream secretKey, final String secretPwd );


    /**
     * *****************************************
     */
    public byte[] decryptAndVerify( byte[] encryptedMessage, final PGPSecretKey secretKey, final String secretPwd,
                                    final PGPPublicKey publicKey );

    ;

    /**
     * *****************************************
     */
    public boolean verify( byte[] signedMessage, PGPPublicKey publicKey );


    /**
     * *****************************************
     */
    public byte[] sign( byte[] message, PGPSecretKey secretKey, String secretPwd, boolean armor );


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
