package io.subutai.core.security.api.crypto;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.security.crypto.pgp.KeyPair;


/**
 * Tool for working with encryption
 */
public interface EncryptionTool
{
    /**
     * Decrypts message with Peer private key
     */
    byte[] decrypt( final byte[] message ) throws PGPException;


    /**
     * Decrypts message with Private Key found by HostId
     */
    byte[] decrypt( final byte[] message, String secretKeyHostId, String pwd ) throws PGPException;


    /* *****************************************
     *
     */
    byte[] decryptAndVerify( final byte[] message, PGPSecretKey secretKey, String pwd, PGPPublicKey pubKey )
            throws PGPException;


    /* *****************************************
     *
     */
    boolean verifyPublicKey( PGPPublicKey keyToVerify, PGPPublicKey keyToVerifyWith );


    /* *****************************************
     *
     */
    PGPPublicKeyRing removeSignature( String id, PGPPublicKeyRing keyToRemoveFrom );


    /* *****************************************
     *
     */
    PGPPublicKeyRing removeSignature( PGPPublicKey keySignToRemove, PGPPublicKeyRing keyToRemoveFrom );


    /* *****************************************
     *
     */
    byte[] decryptAndVerify( final byte[] message, String secretKeyHostId, String pwd, String publicKeyHostId )
            throws PGPException;


    /* **********************************************
     *
     */
    byte[] decrypt( final byte[] message, PGPSecretKeyRing keyRing, String pwd ) throws PGPException;


    /* *****************************************
     *
     */
    byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored );


    /* *****************************************
     *
     */
    byte[] encrypt( final byte[] message, final String publicKeyHostId, boolean armored );


    /* *****************************************
     *
     */
    boolean verify( byte[] signedMessage, PGPPublicKey publicKey );


    /**
     * Signs message with peer private key and encrypts with the given pub key
     *
     * @param message - message
     * @param publicKey - encryption key
     * @param armored - output in armored format
     */
    byte[] signAndEncrypt( final byte[] message, final PGPPublicKey publicKey, final boolean armored )
            throws PGPException;


    /**
     * Signs message with peer private key and encrypts with the given pub key
     *
     * @param message - message
     * @param secretKey - encryption key
     * @param secretPwd - encryption key
     * @param publicKey - encryption key
     * @param armored - output in armored format
     */
    byte[] signAndEncrypt( final byte[] message, PGPSecretKey secretKey, String secretPwd, final PGPPublicKey publicKey,
                           final boolean armored ) throws PGPException;


    /* *********************************************************************
     *
     */
    byte[] signAndEncrypt( final byte[] message, String secretKeyHostId, String secretPwd, final String publicKeyHostId,
                           final boolean armored ) throws PGPException;


    /* *********************************************************************
     *
     */
    byte[] clearSign( final byte[] message, PGPSecretKey secretKey, String secretPwd ) throws PGPException;


    /* *********************************************************************
     *
     */
    boolean verifyClearSign( final byte[] message, PGPPublicKeyRing pgpRings ) throws PGPException;


    /* *********************************************************************
     *
     */
    byte[] extractClearSignContent( final byte[] signedMessage ) throws PGPException;


    /**
     * Decrypts message with peer private key
     *
     * @param encryptedMessage - message
     *
     * @return - {@code ContentAndSignatures}
     */
    ContentAndSignatures decryptAndReturnSignatures( final byte[] encryptedMessage ) throws PGPException;


    /**
     * Verifies the content with its signatures
     *
     * @param contentAndSignatures -  {@code ContentAndSignatures}
     * @param publicKey -  key to verify signatures
     *
     * @return - true if verified successfully, false otherwise
     */

    boolean verifySignature( ContentAndSignatures contentAndSignatures, PGPPublicKey publicKey ) throws PGPException;


    /**
     * Generated keypair
     *
     * @return - KeyPair
     */
    KeyPair generateKeyPair( String userId, String secretPwd, boolean armored );


    /**
     * Signs a  key
     *
     * @param publicKeyRing a  key ring containing the single  key to sign
     * @param id the id we are certifying against the  key
     * @param secretKey the signing key
     * @param secretKeyPassword the signing key password
     *
     * @return a  key ring with the signed  key
     */

    PGPPublicKeyRing signPublicKey( PGPPublicKeyRing publicKeyRing, String id, PGPSecretKey secretKey,
                                    String secretKeyPassword );


    /**
     * Verifies that a  key is signed with another  key
     *
     * @param keyToVerify the  key to verify
     * @param id the id we are verifying against the  key
     * @param keyToVerifyWith the key to verify with
     *
     * @return true if verified, false otherwise
     */
    boolean verifyPublicKey( PGPPublicKey keyToVerify, String id, PGPPublicKey keyToVerifyWith );


    String armorByteArrayToString( byte[] array ) throws PGPException;
}
