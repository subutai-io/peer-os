package io.subutai.core.security.impl.crypto;


import java.io.InputStream;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.core.security.api.crypto.EncryptionTool;


/**
 * Implementation of EncryptionTool
 */
public class EncryptionToolImpl implements EncryptionTool
{
    private static final Logger LOG = LoggerFactory.getLogger( EncryptionToolImpl.class );


    /********************************************
     *
     */
    public EncryptionToolImpl()
    {

    }

    /********************************************
     *
     */
    @Override
    public byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored )
    {
        try
        {
            return PGPEncryptionUtil.encrypt( message,publicKey,armored );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /********************************************
     *
     */
    @Override
    public byte[] decrypt( final byte[] message, final InputStream secretKey, final String secretPwd )
    {
        try
        {
            return PGPEncryptionUtil.decrypt( message, secretKey, secretPwd );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /********************************************
     *
     */
    @Override
    public byte[] decryptAndVerify( byte[] encryptedMessage, final PGPSecretKey secretKey,
                                           final String secretPwd, final PGPPublicKey publicKey )
    {
        try
        {
            return PGPEncryptionUtil.decryptAndVerify( encryptedMessage, secretKey, secretPwd,publicKey );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /********************************************
     *
     */
    @Override
    public byte[] signAndEncrypt( final byte[] message, final PGPSecretKey secretKey, final String secretPwd,
                                         final PGPPublicKey publicKey, final boolean armored )
    {
        try
        {
            return PGPEncryptionUtil.signAndEncrypt( message, secretKey, secretPwd, publicKey, armored );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }



    /********************************************
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



    /********************************************
     *
     */
    @Override
    public byte[] sign( byte[] message, PGPSecretKey secretKey, String secretPwd, boolean armor )
    {
        try
        {
            return PGPEncryptionUtil.sign( message, secretKey, secretPwd, armor );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }
}
