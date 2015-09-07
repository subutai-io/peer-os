package io.subutai.core.security.impl.crypto;


import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public byte[] decrypt( final byte[] message, PGPSecretKeyRing keyRing , String pwd) throws PGPException
    {
        return PGPEncryptionUtil
                .decrypt( message,keyRing, pwd );
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
}
