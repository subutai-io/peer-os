package io.subutai.bazaar.share.pgp.key;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.util.encoders.Hex;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


public class PGPKeyHelper
{
    static
    {
        Security.addProvider( new BouncyCastleProvider() );
    }

    private PGPKeyHelper()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static PGPPublicKey readPublicKey( InputStream is ) throws IOException, PGPException
    {
        PGPPublicKeyRingCollection pgpPub =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( is ), new JcaKeyFingerprintCalculator() );

        Iterator keyRingIter = pgpPub.getKeyRings();

        while ( keyRingIter.hasNext() )
        {
            PGPPublicKeyRing keyRing = ( PGPPublicKeyRing ) keyRingIter.next();
            Iterator keyIter = keyRing.getPublicKeys();

            while ( keyIter.hasNext() )
            {
                PGPPublicKey key = ( PGPPublicKey ) keyIter.next();

                if ( key.isEncryptionKey() )
                {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException( "Can't find encryption key in key ring." );
    }


    public static PGPPublicKey readPublicKey( String filePath ) throws IOException, PGPException
    {
        PGPPublicKey publicKey;

        try ( InputStream is = new FileInputStream( filePath ) )
        {
            publicKey = readPublicKey( is );
        }

        return publicKey;
    }


    public static PGPPublicKey readPublicKeyFromString( String key ) throws IOException, PGPException
    {
        if ( StringUtils.isBlank( key ) )
        {
            throw new IllegalArgumentException( "Public key string is null" );
        }

        return readPublicKey( IOUtils.toInputStream( key ) );
    }


    private static PGPSecretKey readSecretKey( InputStream is ) throws IOException, PGPException
    {
        PGPSecretKeyRingCollection pgpSec =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( is ), new JcaKeyFingerprintCalculator() );
        Iterator keyRingIter = pgpSec.getKeyRings();

        while ( keyRingIter.hasNext() )
        {
            PGPSecretKeyRing keyRing = ( PGPSecretKeyRing ) keyRingIter.next();
            Iterator keyIter = keyRing.getSecretKeys();

            while ( keyIter.hasNext() )
            {
                PGPSecretKey key = ( PGPSecretKey ) keyIter.next();

                if ( key.isSigningKey() )
                {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException( "Can't find signing key in key ring." );
    }


    public static PGPPrivateKey readPrivateKey( InputStream is, String password ) throws PGPException, IOException
    {
        PGPSecretKey secretKey = readSecretKey( is );

        return secretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider( "BC" ).build( password.toCharArray() ) );
    }


    public static PGPPrivateKey readPrivateKey( String filePath, String password ) throws PGPException, IOException
    {
        PGPPrivateKey privateKey;

        try ( InputStream is = new FileInputStream( filePath ) )
        {
            privateKey = readPrivateKey( is, password );
        }

        return privateKey;
    }


    public static String getFingerprint( PGPPublicKey publicKey )
    {
        return Hex.toHexString( publicKey.getFingerprint() );
    }


    public static String getOwnerString( PGPPublicKey publicKey )
    {
        return ( String ) publicKey.getUserIDs().next();
    }
}
