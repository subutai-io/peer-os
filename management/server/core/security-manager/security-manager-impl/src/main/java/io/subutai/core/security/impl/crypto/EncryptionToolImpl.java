package io.subutai.core.security.impl.crypto;


import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.impl.model.SecurityKeyData;


/**
 * Implementation of EncryptionTool
 */
public class EncryptionToolImpl implements EncryptionTool
{
    private static final Logger LOG = LoggerFactory.getLogger( EncryptionToolImpl.class );

    private final KeyManagerImpl keyManager;


    /* *****************************************
     *
     */
    public EncryptionToolImpl( KeyManagerImpl keyManager )
    {
        this.keyManager = keyManager;
    }


    /* *****************************************
     *
     */
    @Override
    public byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored )
    {
        try
        {
            return PGPEncryptionUtil.encrypt( message, publicKey, armored );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *****************************************
     *
     */
    @Override
    public boolean verify( byte[] signedMessage, PGPPublicKey publicKey )
    {
        try
        {
            return PGPEncryptionUtil.verify( signedMessage, publicKey );
        }
        catch ( Exception ex )
        {
            return false;
        }
    }


    /* *****************************************
     *
     */
    @Override
    public byte[] signAndEncrypt( final byte[] message, final PGPPublicKey publicKey, final boolean armored )
            throws PGPException
    {

        return PGPEncryptionUtil
                .signAndEncrypt( message, keyManager.getSecretKey( null ), keyManager.getSecurityKeyData().getSecretKeyringPwd(),
        publicKey, armored);
    }


    /* *****************************************
     *
     */
    @Override
    public byte[] decrypt( final byte[] message ) throws PGPException
    {
        return PGPEncryptionUtil
                .decrypt( message,keyManager.getSecretKeyRingInputStream( null ), keyManager.getSecurityKeyData().getSecretKeyringPwd() );
    }


    /* *****************************************
     *
     */
    @Override
    public byte[] decryptAndVerify( final byte[] message,PGPSecretKey secretKey, String pwd ,PGPPublicKey pubKey) throws PGPException
    {
        if( Strings.isNullOrEmpty(pwd))
        {
            pwd = keyManager.getSecurityKeyData().getSecretKeyringPwd();
        }

        return PGPEncryptionUtil.decryptAndVerify( message, secretKey, pwd, pubKey );
    }


    /* *****************************************
     *
     */
    @Override
    public byte[] decrypt( final byte[] message, PGPSecretKeyRing keyRing , String pwd) throws PGPException
    {
        if( Strings.isNullOrEmpty(pwd))
        {
            pwd = keyManager.getSecurityKeyData().getSecretKeyringPwd();
        }

        return PGPEncryptionUtil.decrypt( message, keyRing, pwd );
    }


    /* *****************************************
     *
     */
    @Override
    public ContentAndSignatures decryptAndReturnSignatures( final byte[] encryptedMessage ) throws PGPException
    {
        return PGPEncryptionUtil.decryptAndReturnSignatures( encryptedMessage, keyManager.getSecretKey( null ),
                keyManager.getSecurityKeyData().getSecretKeyringPwd() );
    }


    /* *****************************************
     *
     */
    @Override
    public boolean verifySignature( final ContentAndSignatures contentAndSignatures, final PGPPublicKey publicKey )
            throws PGPException
    {
        return PGPEncryptionUtil.verifySignature( contentAndSignatures, publicKey );
    }


    /* *****************************************
    *
    */
    @Override
    public KeyPair generateKeyPair ( String userId,String secretPwd, boolean armored )
    {
        KeyPair keyPair = null;

        try
        {
            keyPair = PGPEncryptionUtil.generateKeyPair(userId,secretPwd,armored);
            return keyPair;
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    /**
     * Signs a public key
     *
     * @param publicKeyRing a public key ring containing the single public key to sign
     * @param id the id we are certifying against the public key
     * @param secretKey the signing key
     * @param secretKeyPassword the signing key password
     *
     * @return a public key ring with the signed public key
     */
    @Override
    public  PGPPublicKeyRing signPublicKey( PGPPublicKeyRing publicKeyRing, String id, PGPSecretKey secretKey, String secretKeyPassword )
    {
        try
        {
            if( Strings.isNullOrEmpty(secretKeyPassword))
            {
                secretKeyPassword = keyManager.getSecurityKeyData().getSecretKeyringPwd();
            }

            return PGPEncryptionUtil.signPublicKey( publicKeyRing, id, secretKey, secretKeyPassword  );
        }
        catch ( Exception e )
        {
            //throw custom  exception
            throw new RuntimeException( e );
        }
    }


    /**
     * Verifies that a public key is signed with another public key
     *
     * @param keyToVerify the public key to verify
     * @param id the id we are verifying against the public key
     * @param keyToVerifyWith the key to verify with
     *
     * @return true if verified, false otherwise
     */
    @Override
    public  boolean verifyPublicKey( PGPPublicKey keyToVerify, String id, PGPPublicKey keyToVerifyWith )
    {
        try
        {
            return PGPEncryptionUtil.verifyPublicKey(  keyToVerify, id, keyToVerifyWith  );
        }
        catch ( Exception e )
        {
            //throw custom  exception
            throw new RuntimeException( e );
        }
    }
}
