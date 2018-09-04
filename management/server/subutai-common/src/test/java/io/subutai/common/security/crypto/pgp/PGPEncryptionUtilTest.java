package io.subutai.common.security.crypto.pgp;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;


public class PGPEncryptionUtilTest
{
    private static final Logger logger = LoggerFactory.getLogger( PGPEncryptionUtilTest.class );

    private static final String MESSAGE = "hello";
    protected static final String PUBLIC_KEYRING = "dummy.pkr";
    protected static final String SECRET_KEYRING = "dummy.skr";

    private static final String PLUGIN_PUBLIC_KEY = "public.asc";
    private static final String PLUGIN_PUBLIC_KEY_FINGERPRINT = "FC1F3E708FD85E7431FA996D8DD661979B15E409";
    private static final String PLUGIN_PRIVATE_KEY = "private.asc";
    private static final String PLUGIN_PRIVATE_KEY_FINGERPRINT = "FC1F3E708FD85E7431FA996D8DD661979B15E409";

    private static final String SECRET_PWD = "12345678";
    protected static final String PUBLIC_KEY_ID = "e2451337c277dbf1";
    private static final String SECRET_KEY_ID = "d558f9a4a0b450b3";
    private static final String USER_ID = "user@mail.org";
    private static final String PUBLIC_KEY_FINGERPRINT = "8338133EF14DE47D4B1646BEE2451337C277DBF1";
    private static final String SECRET_KEY_FINGERPRINT = "3E5DB4DCF15A31C93CF3C9D8D558F9A4A0B450B3";


    @Test
    public void testSignVerify() throws Exception
    {
        PGPSecretKey secretKey = PGPEncryptionUtil.findSecretKeyById( findFile( SECRET_KEYRING ), SECRET_KEY_ID );
        byte[] signedMessage = PGPEncryptionUtil.sign( MESSAGE.getBytes(), secretKey, SECRET_PWD, true );

        assertTrue( PGPEncryptionUtil.verify( signedMessage, secretKey.getPublicKey() ) );
    }


    public static InputStream findFile( final String file )
    {
        return PGPEncryptionUtilTest.class.getClassLoader().getResourceAsStream( file );
    }


    @Test
    public void testEncryptAndDecrypt() throws Exception
    {
        byte[] encryptedMessage = PGPEncryptionUtil.encrypt( MESSAGE.getBytes(),
                PGPEncryptionUtil.findPublicKeyById( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_ID ), true );

        byte[] decryptedMessage = PGPEncryptionUtil.decrypt( encryptedMessage, findFile( SECRET_KEYRING ), SECRET_PWD );

        assertTrue( Arrays.equals( MESSAGE.getBytes(), decryptedMessage ) );
    }


    @Test
    public void testSignEncryptAndDecryptVerify() throws Exception
    {
        PGPSecretKey signingKey =
                PGPEncryptionUtil.findSecretKeyByFingerprint( findFile( SECRET_KEYRING ), SECRET_KEY_FINGERPRINT );
        PGPPublicKey encryptingKey =
                PGPEncryptionUtil.findPublicKeyByFingerprint( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_FINGERPRINT );

        byte[] signedAndEncryptedMessage =
                PGPEncryptionUtil.signAndEncrypt( MESSAGE.getBytes(), signingKey, SECRET_PWD, encryptingKey, true );

        PGPSecretKey decryptingSecretKey = PGPEncryptionUtil.findSecretKeyByFingerprint( findFile( SECRET_KEYRING ),
                PGPEncryptionUtil.BytesToHex( encryptingKey.getFingerprint() ) );

        byte[] decryptedAndVerifiedMessage = PGPEncryptionUtil
                .decryptAndVerify( signedAndEncryptedMessage, decryptingSecretKey, SECRET_PWD,
                        signingKey.getPublicKey() );

        assertTrue( Arrays.equals( MESSAGE.getBytes(), decryptedAndVerifiedMessage ) );
    }


    @Test
    public void testVerifySignature() throws Exception
    {
        PGPPublicKey encryptingKey =
                PGPEncryptionUtil.findPublicKeyByFingerprint( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_FINGERPRINT );

        PGPSecretKeyRing secretKeys = PGPKeyUtil.readSecretKeyRing( findFile( SECRET_KEYRING ) );


        byte[] signedAndEncryptedMessage = PGPEncryptionUtil
                .signAndEncrypt( MESSAGE.getBytes(), secretKeys.getSecretKey(), SECRET_PWD, encryptingKey, true );

        ContentAndSignatures contentAndSignatures =
                PGPEncryptionUtil.decryptAndReturnSignatures( signedAndEncryptedMessage, secretKeys, SECRET_PWD );

        assertTrue( PGPEncryptionUtil.verifySignature( contentAndSignatures, secretKeys.getPublicKey() ) );
    }


    @Test
    public void testArmorByteArray() throws Exception
    {

        String armored = new String( PGPEncryptionUtil.armorByteArray( "test".getBytes() ) );

        assertThat( armored, startsWith( "-----BEGIN PGP MESSAGE-----" ) );
    }


    @Test
    public void testGetPrivateKey() throws Exception
    {
        PGPSecretKey secretKey =
                PGPEncryptionUtil.findSecretKeyByFingerprint( findFile( SECRET_KEYRING ), SECRET_KEY_FINGERPRINT );

        assertNotNull( PGPEncryptionUtil.getPrivateKey( secretKey, SECRET_PWD ) );

        assertNull( PGPEncryptionUtil.getPrivateKey( secretKey, "" ) );
    }


    @Test
    public void testArmorByteArrayToString() throws Exception
    {
        String armored = PGPEncryptionUtil.armorByteArrayToString( "test".getBytes() );

        assertThat( armored, startsWith( "-----BEGIN PGP MESSAGE-----" ) );
    }


    @Test
    public void testGenerateKeyPair() throws Exception
    {
        KeyPair keyPair = PGPEncryptionUtil.generateKeyPair( USER_ID, SECRET_PWD, false );

        assertNotNull( PGPEncryptionUtil
                .findPublicKeyById( new ByteArrayInputStream( keyPair.getPubKeyring() ), keyPair.getSubKeyId() ) );
        assertNotNull( PGPEncryptionUtil
                .findSecretKeyByFingerprint( new ByteArrayInputStream( keyPair.getSecKeyring() ),
                        keyPair.getPrimaryKeyFingerprint() ) );
    }


    @Test
    public void testGetX509CertificateFromPgpKeyPair() throws Exception
    {

        Date today = new Date();
        PGPPublicKey pgpPublicKey = PGPEncryptionUtil.findPublicKeyById( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_ID );
        PGPSecretKey pgpSecretKey = PGPEncryptionUtil.findSecretKeyById( findFile( SECRET_KEYRING ), SECRET_KEY_ID );
        X509Certificate x509Certificate = PGPEncryptionUtil
                .getX509CertificateFromPgpKeyPair( pgpPublicKey, pgpSecretKey, SECRET_PWD,
                        "C=ZA, ST=Western Cape, L=Cape Town, O=Thawte Consulting cc,"
                                + " OU=Certification Services Division,"
                                + " CN=Thawte Server CA/emailAddress=server-certs@thawte.com",
                        "C=US, ST=Maryland, L=Pasadena, O=Brent Baccala,"
                                + "OU=FreeSoft, CN=www.freesoft.org/emailAddress=baccala@freesoft.org",

                        today, new Date( today.getTime() + ( 1000 * 60 * 60 * 24 ) ), new BigInteger( "1" ) );

        assertNotNull( x509Certificate );


        JcaPGPKeyConverter c = new JcaPGPKeyConverter();
        PublicKey publicKey = c.getPublicKey( pgpSecretKey.getPublicKey() );
        x509Certificate.verify( publicKey, new BouncyCastleProvider() );
    }


    @Test
    public void testVerifyKey() throws Exception
    {

        PGPPublicKeyRing pgpPublicKey = PGPKeyUtil.readPublicKeyRing( findFile( PUBLIC_KEYRING ) );
        PGPSecretKey pgpSecretKey = PGPEncryptionUtil.findSecretKeyById( findFile( SECRET_KEYRING ), SECRET_KEY_ID );

        PGPPublicKeyRing signedPubKey =
                PGPEncryptionUtil.signPublicKey( pgpPublicKey, USER_ID, pgpSecretKey, SECRET_PWD );

        assertTrue( PGPEncryptionUtil
                .verifyPublicKey( signedPubKey.getPublicKey(), USER_ID, pgpSecretKey.getPublicKey() ) );
    }


    @Test
    public void testKeySigning() throws PGPException, IOException
    {
        KeyPair first = PGPEncryptionUtil.generateKeyPair( "first@key.com", "first", false );
        KeyPair second = PGPEncryptionUtil.generateKeyPair( "second@key.com", "second", false );
        signKeyAndPrintIds( first, second, "second" );

        InputStream firstPublicStream = new ByteArrayInputStream( first.getPubKeyring() );
        InputStream secondPublicStream = new ByteArrayInputStream( second.getPubKeyring() );

        PGPPublicKeyRingCollection firstPublicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( firstPublicStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPPublicKeyRingCollection secondPublicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( secondPublicStream ),
                        new JcaKeyFingerprintCalculator() );

        if ( firstPublicKeyRingCollection.getKeyRings().hasNext() )
        {
            PGPPublicKeyRing firstPublicKeyRing = null;
            PGPPublicKeyRing secondPublicKeyRing = null;
            firstPublicKeyRing = firstPublicKeyRingCollection.getKeyRings().next();
            secondPublicKeyRing = secondPublicKeyRingCollection.getKeyRings().next();
            assertEquals( true,
                    printPublicKeySignatures( firstPublicKeyRing.getPublicKey(), secondPublicKeyRing.getPublicKey() ) );
        }
    }


    private void signKeyAndPrintIds( KeyPair first, KeyPair second, String password ) throws IOException, PGPException
    {
        InputStream firstPublicStream = new ByteArrayInputStream( first.getPubKeyring() );
        InputStream secondPublicStream = new ByteArrayInputStream( second.getPubKeyring() );
        InputStream secondSecretStream = new ByteArrayInputStream( second.getSecKeyring() );

        PGPPublicKeyRingCollection keyrings =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( firstPublicStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPPublicKeyRing firstPublicKeyRing = null;
        if ( keyrings.getKeyRings().hasNext() )
        {
            firstPublicKeyRing = keyrings.getKeyRings().next();


            PGPSecretKey secondSecretKey =
                    PGPEncryptionUtil.findSecretKeyById( secondSecretStream, second.getPrimaryKeyId() );
            PGPPublicKey secondPublicKey =
                    PGPEncryptionUtil.findPublicKeyById( secondPublicStream, second.getPrimaryKeyId() );

            if ( secondSecretKey != null )
            {
                String keyId = Long.toHexString( secondSecretKey.getKeyID() );

                PGPPublicKeyRing firstSignedPublicKeyRing =
                        PGPEncryptionUtil.signPublicKey( firstPublicKeyRing, keyId, secondSecretKey, password );

                printPublicKeySignatures( firstSignedPublicKeyRing.getPublicKey(), secondPublicKey );

                first.setPubKeyring( firstSignedPublicKeyRing.getEncoded() );
            }
        }
    }


    private boolean printPublicKeySignatures( PGPPublicKey publicKey, final PGPPublicKey secondPublicKey )
    {
        boolean verification = false;
        try
        {
            verification = PGPEncryptionUtil
                    .verifyPublicKey( publicKey, Long.toHexString( secondPublicKey.getKeyID() ), secondPublicKey );
        }
        catch ( PGPException e )
        {
            e.printStackTrace();
        }
        logger.info( "%%%%%%%%%%%%% Signature verification: " + verification );
        Iterator keySignatures = publicKey.getSignatures();
        while ( keySignatures.hasNext() )
        {
            PGPSignature signature = ( PGPSignature ) keySignatures.next();
            signature.getSignatureType();
            logger.info( Long.toHexString( signature.getKeyID() ) );
        }
        return verification;
    }


    @Test
    public void testMessageSigning() throws Exception
    {
        InputStream secondSecretStream = findFile( PLUGIN_PRIVATE_KEY );
        InputStream secondPublicStream = findFile( PLUGIN_PUBLIC_KEY );

        PGPSecretKeyRingCollection secretKeyRingCollection =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( secondSecretStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPSecretKeyRing secretKeyRing = secretKeyRingCollection
                .getSecretKeyRing( secretKeyRingCollection.iterator().next().getSecretKey().getKeyID() );

        PGPSecretKey secondSecretKey = secretKeyRing.getSecretKey();

        PGPPublicKeyRingCollection secondPublicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( secondPublicStream ),
                        new JcaKeyFingerprintCalculator() );


        PGPPublicKeyRing pgpKeyring = secondPublicKeyRingCollection
                .getPublicKeyRing( secondPublicKeyRingCollection.iterator().next().getPublicKey().getKeyID() );


        byte[] encryptedMessage =
                PGPEncryptionUtil.encrypt( "Test message.\n".getBytes(), pgpKeyring.getPublicKey(), true );

        byte[] signedMessageArmor =
                PGPEncryptionUtil.clearSign( encryptedMessage, secondSecretKey, "123".toCharArray(), "" );

        String signedMessage = new String( signedMessageArmor, StandardCharsets.UTF_8 );

        logger.info( "\n" + signedMessage );
        logger.info( "\n======================" );

        boolean result = PGPEncryptionUtil.verifyClearSign( signedMessageArmor, pgpKeyring );
        if ( result )
        {
            logger.info( "signature verified." );
        }
        else
        {
            logger.info( "signature verification failed." );
        }

        byte[] extracted = PGPEncryptionUtil.extractContentFromClearSign( signedMessage.getBytes() );
        byte[] decrypted = PGPEncryptionUtil.decrypt( extracted, secretKeyRing, "123" );
        logger.info( "Decrypted message \n" + new String( decrypted, StandardCharsets.UTF_8 ) );

        assertEquals( true, result );
    }


    @Test
    public void testClearSign() throws Exception
    {
        InputStream secondSecretStream = findFile( PLUGIN_PRIVATE_KEY );
        InputStream secondPublicStream = findFile( PLUGIN_PUBLIC_KEY );

        PGPSecretKeyRingCollection secretKeyRingCollection =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( secondSecretStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPSecretKeyRing secretKeyRing = secretKeyRingCollection
                .getSecretKeyRing( secretKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        PGPSecretKey secondSecretKey = secretKeyRing.getSecretKey();

        PGPPublicKeyRingCollection secondPublicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( secondPublicStream ),
                        new JcaKeyFingerprintCalculator() );


        PGPPublicKeyRing pgpKeyring = secondPublicKeyRingCollection
                .getPublicKeyRing( secondPublicKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        byte[] signedMessageArmor = PGPEncryptionUtil
                .clearSign( IOUtils.toString( findFile( "message.txt" ) ).getBytes(), secondSecretKey,
                        "123".toCharArray(), "" );

        String signedMessage = new String( signedMessageArmor, StandardCharsets.UTF_8 );

        logger.info( "\n" + signedMessage );

        boolean result = PGPEncryptionUtil.verifyClearSign( signedMessage.getBytes(), pgpKeyring );
        if ( result )
        {
            logger.info( "signature verified." );
        }
        else
        {
            logger.info( "signature verification failed." );
        }

        assertEquals( true, result );
    }


    @Test
    public void testVerifyClearSign() throws Exception
    {
        InputStream secondPublicStream = findFile( PLUGIN_PUBLIC_KEY );
        PGPPublicKeyRingCollection secondPublicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( secondPublicStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPPublicKeyRing pgpKeyring = secondPublicKeyRingCollection
                .getPublicKeyRing( secondPublicKeyRingCollection.iterator().next().getPublicKey().getKeyID() );

        String signedMessage = IOUtils.toString( findFile( "signedMessage.txt" ) );

        logger.info( "\n" + signedMessage );

        boolean result = PGPEncryptionUtil.verifyClearSign( signedMessage.getBytes(), pgpKeyring );
        if ( result )
        {
            logger.info( "signature verified." );
        }
        else
        {
            logger.info( "signature verification failed." );
        }

        assertEquals( true, result );
    }


    @Test
    public void testExtractingContentFromClearSign()
    {
        PGPPublicKey key = null;
        try
        {
            InputStream in = findFile( PLUGIN_PRIVATE_KEY );
            in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream( in );

            JcaPGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection( in );
            in.close();


            Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
            while ( key == null && rIt.hasNext() )
            {
                PGPPublicKeyRing kRing = rIt.next();
                Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
                while ( key == null && kIt.hasNext() )
                {
                    PGPPublicKey k = kIt.next();

                    if ( k.isEncryptionKey() )
                    {
                        key = k;
                    }
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    @Test
    public void verifySignature() throws Exception
    {
        String message = getSignedMessage();
        PGPPublicKeyRing pubKeyRing = PGPKeyUtil.readPublicKeyRing( getPublicKey() );

        assertTrue( PGPEncryptionUtil.verifyClearSign( message.getBytes(), pubKeyRing ) );
    }


    private String getSignedMessage()
    {
        String messageSigned = "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n" + "\n"
                + "76bfb0d6-bc3a-4ed3-9078-d64e104aaec8\n" + "-----BEGIN PGP SIGNATURE-----\n"
                + "Version: GnuPG v2.0.22 (GNU/Linux)\n" + "\n"
                + "iQIcBAEBAgAGBQJXNC/UAAoJELIbGGHrOj4+8vcP/j1L0YaCnN3aYhSWyWYrHvv8\n"
                + "Jk83ySuZZ7ngyzqs6mj2Tnv/PFFDO1EDN2F1mNdAX/vOdfie8jL6D60ugteMvbFh\n"
                + "0OZUklmYCQpJ+9THUpqn0xyJ2BbuPA7eO5socSKSZB67fTzBlYY1kANQdjbGYu7H\n"
                + "V1n81o3iUQqQE4wwwlaerwFnwgrsOc1Sc9+ELIrjwFveh7gRfTqPx1LY7MnPUQt/\n"
                + "MCfnCAky6cNjTFIZB6yIJg0C98NFuLn0uk9nE9nxFLrPgRgFOLbAfbhp0Hyyn18/\n"
                + "embpJHjK+6O7PsmXLqirzw8d22AMHCGpWBPluZGyKXAnnaVVON4qd6k2yoJwZSTB\n"
                + "C5IaKAKFofudDG1h4vicVBeURF7ZDqR2DCs0LiUHS7kfLfc73RFriH/6GeCVELqV\n"
                + "8C32BFg0Qq6nGOnzPtzzzJquo4RTWfwmzsNWPtEB30DPzpyKfZKrtPcwl0lcwhEs\n"
                + "JglD2yU46CT4zoWW9pHQ4AGvwYAqn7nbkig4v5AlEnkTWN75d4bZj4G5b+iCCnm8\n"
                + "TUQLK6abVOe0JvYcj7stkPlQ8xMsBSZ0j8oVGf+CLw5M6xIMIJF5Xou6Yljvm5Uz\n"
                + "GdLec+eMzjX7qZbAgsVZUrzYnt7VwvBMS4aPl38uaxtznXWn5wCFyKW7AFoH5Tfh\n" + "oOLTyRYdbjXsjjwSXQqf\n"
                + "=sIF7\n" + "-----END PGP SIGNATURE-----\n";

        return messageSigned.trim();
    }


    private String getPublicKey()
    {
        String pubKeySTR = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" + "Version: GnuPG v2.0.22 (GNU/Linux)\n" + "\n"
                + "mQINBFcwIFYBEADHoXrLzLEekfJN1yEeXSawRdXe3KZTtZpaL+8ALCL2nDdPjtDh\n"
                + "2gavkyMNVnR7YfhJP6YBsGWgfqCJVgkR0kF+Y18qIFI6E+vRwlB+Jq95vZNQuvJK\n"
                + "iJ+wIdcPET2cGQUrlEwB73skd25TmukDbruZuuyVAIWOAJPoSRV7WYf/t3nBtMkJ\n"
                + "5ZHJy/jOvhSGptE1xSARcpLc43jHkxHIak57GTsN+CldfGcIPXQfr8yrIUfyzbf9\n"
                + "qLqB9UpU4We6QXckfSXVUtpp80kg1F2szLzQ+DSHYAEOpq9e+rxd4/61ElZKJHGT\n"
                + "KjdS/asg4zlMLjEhDnG5Drdj8WrKvHd8CGOjbHW9497qVk3W++E3Q6Yx8kvVZECV\n"
                + "KvmzlzrGWGxvveuAFhYqH0nnbkdFa/QAkjSibLz10cu4zTHl5utN9cHm6swDmZEJ\n"
                + "okxdmh5bugZ3rUI3nYmWo7C/yTvRwRDwnmkyCUyr5G73ElBZ5xTnPZWtYWBO+zrb\n"
                + "6rSqYLw5oR59zSBlcQwQVGbD6Xj+eEgpImNGNf5YvGM18TUN5ODHeDIbGSoltLK0\n"
                + "1fM/ymMYHI4DOMaoWFoxW0pl2WVN3axfaBje6lYlCecWRChtIybfPCC79YdYwVSr\n"
                + "eW1eK5OGIMNTYkDZUwAlN6D5BRHuNnV4OBJP+9zFnDociyoGya9sk47wnQARAQAB\n"
                + "tA9uayA8bmtAb3B0LmNvbT6JAjUEEAEIACkFAlcwIF0GCwkIBwMCCRCyGxhh6zo+\n"
                + "PgQVCAIKAxYCAQIZAQIbAwIeAQAA1MgP/Rez2jHsFI/x0INYGeTZ/M27U7pMlJ3h\n"
                + "hzkzjdfnc4bjfE3Ez4oEt5k0IpVXnmtajKZY/yflK66YTt2BfW11sXSWvs1tVSmE\n"
                + "ib9U+Gw+SpUC+L6Ne19N8ZiqSojBk7m5Y+AqZgzkOG1DzAe+YJbG8oFpOgl7NiJn\n"
                + "mx/Bw/JkVjscIPYlvDEYXCmbmSNTjRiYpXDP2E+EilbPFVzv8ZDYUVHypDVyXokf\n"
                + "wtMHVjWVjyCy0LA6hfQRUJDMFUi2Yj95TyAywbEo0CwXNXqSr4FM3F6c6InSWnHv\n"
                + "UMfsPmxgAByHAjPlfTE25ZhoYxdB7kfjPeA5rjv4aH02WU7EAFItAcxklqpL4xHJ\n"
                + "yY10nGccc4bqWS6UadPXGVofU+dgPFGBuKWPuE3psZPDUxTeioZ8qtNVgMgaitV6\n"
                + "IZygpagdVbyPCd1xQ8tUn3REUTP9EoQhVcolm55zMJphDtZkcVoo5GRqpjNFB9ad\n"
                + "4ZNsAIh0FEyphlzp20ZN5Efz4rguKjWvyGenbJlWWTx4lJ0jRXLiZovgU/3QEJPW\n"
                + "pJ3I23PaDPjlUkRfBX8iGwkV3z3MaTyQBz+2rgy/Ltr75PpqdzOe5bhJ3FPurYoo\n"
                + "GKy190VmZf9Zx4M8Fk/fp2LWnbNl4xxJNjwHjxjf75olBdg//gsRFqtGJip53J5O\n"
                + "tDUgzqrsH+JWuQINBFcwIFYBEADBoXuDBfAYLmBh+tb/6QfnpxLC7QInSIHalIkl\n"
                + "d1MTCjgzjvzi2YYsacxa92kvFSnNG/pW/owCgCUp8x7QHgebQCX3yTlngC8rRxt5\n"
                + "oCXmMO89ODxx4GqCu61VyLEHouRUoL0TfIai7H4WX3LL3+uAunzycBgAghOvFnle\n"
                + "08At426EtfNZI5KPUAa2C61wlMEClN1cAm14csE3DF17XjoVN3BGco5tlkZ9t6Fa\n"
                + "cBRhfD1ZDnIu2F1PLM2lgtn+dzWUp559I7S72CBKFvzKr/+x4wAt8ALlPrS7uA5o\n"
                + "3QrwqdAdXtiJc5BbJxI4tXp034IcK8bH5hmtkzjoU0hfc4OAwNOTIRdOlhuP5RnC\n"
                + "MILBoK7WCE48u+JqyHk2Wq/Q1CwM5qb8+fEUMjJ8o+g2zxuOXj46HFLbXBYznRGL\n"
                + "rSvpGnjgQQLbovBSHMUthCGdDJNNjDaeqSWrkc+uvGqBSggoCkB91VjvT6ei5qK3\n"
                + "PGappcVrtZFur50HFYf8eoNaZKKFva0qAY9xWfP07VBLjoZqh7JzIN3g3si6H5Fq\n"
                + "BqwrQZA3p0bMXmpg+w9nIkr+RZvn0aPJDNrOxtKaobNLe7i9U9OvGoTqWYpVmqcv\n"
                + "Cy/WPziQpelV3OaP7KCWPWMiXDH9KnTnT+9CWpPv+Wt8MVMgQKCYNTBnrxz/M0rq\n"
                + "HDbtsQARAQABiQIfBBgBCAATBQJXMCBfCRCyGxhh6zo+PgIbDAAAPVUP/1FOL2+o\n"
                + "JxQ0L7oBPiSCC1v0jxf7Rrl6kdnxrB16aPEFY0/qGvNUOwGDTi1umYrnVXNmFHTZ\n"
                + "N8pMHZ8k1cBujd7VeNK5rD3+IopdKXABmaljyl1fFfLADu3BSN+B09zWQDj+0f/F\n"
                + "GAkWUKZN4EL1GmuESrYqgqUAUdkLWZaQk45NRQfswyLmm94SynKIj/8lEgLK5+7p\n"
                + "WTw1WGPA1xkcGSTjB2HKyiuHU/5WMmRupQaquA384ePYDROnonxM6nTOrpJXtA9P\n"
                + "TqZ6vSGKBSmwxs4oXL6N35+FWBL4f1cVf1lpMgvyKYfhuG4sEBDjfDYLRP61vW5n\n"
                + "1/EY2vLPsF3pVCgNLqvgYt0bGVX8/kP4Nco253gpBJ+rhaqBgTcxlp99FdoG9DpV\n"
                + "1o+C/bBNaQRM/XFYoVuQTUBx+q7Q0eYs12Np/TQYo8aJta5mamcYNOllmqm1cr51\n"
                + "ekLN0lhRkt20guHXvMKLHyRxjyognGhMxx2W8fWlZFhyXfWEPkfDSs6x2KflAjY/\n"
                + "UL7hekA+RmWriRbSXTo1LWorKcWA9j1RNgsWgj00dSTxovpw6VOrWdsOjrTdXMyz\n"
                + "V8mUkuFQ35xv/nsUzF3xAu2hkzfkay3zY6CvPJjqHNUiVWeKlxFfvcMQ8dN1Jhie\n"
                + "CD+qSAgWwSKkoxPMTAiBBvzNC9X036GfDXFE\n" + "=lNw/\n" + "-----END PGP PUBLIC KEY BLOCK-----";

        return pubKeySTR;
    }
}
