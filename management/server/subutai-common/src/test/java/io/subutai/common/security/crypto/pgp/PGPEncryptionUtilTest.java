package io.subutai.common.security.crypto.pgp;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


public class PGPEncryptionUtilTest
{
    private static final Logger logger = LoggerFactory.getLogger( PGPEncryptionUtilTest.class );

    private static final String MESSAGE = "hello";
    private static final String PUBLIC_KEYRING = "dummy.pkr";
    private static final String SECRET_KEYRING = "dummy.skr";

    private static final String SECRET_PWD = "12345678";
    private static final String PUBLIC_KEY_ID = "e2451337c277dbf1";
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

        //auto secret key detection
        decryptedAndVerifiedMessage = PGPEncryptionUtil
                .decryptAndVerify( signedAndEncryptedMessage, findFile( SECRET_KEYRING ), SECRET_PWD,
                        signingKey.getPublicKey() );

        assertTrue( Arrays.equals( MESSAGE.getBytes(), decryptedAndVerifiedMessage ) );
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
        KeyPair second = PGPEncryptionUtil.generateKeyPair( "second@key.com", "second", false );

        InputStream secondSecretStream = new ByteArrayInputStream( second.getSecKeyring() );
        InputStream secondPublicStream = new ByteArrayInputStream( second.getPubKeyring() );

        PGPSecretKeyRingCollection secretKeyRingCollection =
                new PGPSecretKeyRingCollection( PGPUtil.getDecoderStream( secondSecretStream ),
                        new JcaKeyFingerprintCalculator() );

        PGPSecretKeyRing secretKeyRing =
                secretKeyRingCollection.getSecretKeyRing( new BigInteger( second.getPrimaryKeyId(), 16 ).longValue() );

        PGPSecretKey secondSecretKey = secretKeyRing.getSecretKey();

        PGPPublicKeyRingCollection secondPublicKeyRingCollection =
                new PGPPublicKeyRingCollection( PGPUtil.getDecoderStream( secondPublicStream ),
                        new JcaKeyFingerprintCalculator() );


        PGPPublicKeyRing pgpKeyring = secondPublicKeyRingCollection
                .getPublicKeyRing( new BigInteger( second.getSubKeyId(), 16 ).longValue() );


        byte[] encryptedMessage =
                PGPEncryptionUtil.encrypt( "Talas Zholdoshbekov".getBytes(), pgpKeyring.getPublicKey(), true );

        byte[] signedMessageArmor =
                PGPEncryptionUtil.clearSign( encryptedMessage, secondSecretKey, "second".toCharArray(), "" );

        String signedMessage = new String( signedMessageArmor, "UTF-8" );

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

        byte[] extracted = PGPEncryptionUtil.extractContentFromClearSign( signedMessage.getBytes() );
        byte[] decrypted = PGPEncryptionUtil.decrypt( extracted, secretKeyRing, "second" );
        logger.info( "Decrypted message \n" + new String( decrypted, "UTF-8" ) );

        assertEquals( true, result );
    }


    @Test
    public void testExtractingContentFromClearSign()
    {
        String clearSign =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA256\n" + "\n" + "-----BEGIN PGP MESSAGE-----\n"
                        + "Version: BCPG v1.52\n" + "\n"
                        + "hQEMA2Y9gO5nrCSvAQf9FpDCWPQFDgT42/o1yKb6kmFz1Q5scHR6pbp68Q4lV6LQ\n"
                        + "ou fLFKBPIS1qAkAe2oOyzcylUu3pezeJ GlTJqyNrpEjYNPVuwUeLlqxo1s1QxU\n"
                        + "fZhJ76EPln3csG3EjN9sgjni6sD/3XBLexw0kn9hP OHRtFVvf/vBbi0fyh0 YPu\n"
                        + "qSfboJQokvR JoJ14TITtUqqqdrt/uEmyu3qK/A8QCyRFbLn8J0lKDcuaAmsJ51W\n"
                        + "6IvhVaMF4vE8UF9qU4c8qBL6mWaN4B1IImYI2UOrzL8DCNOnijyD1RVINcqCpAFo\n"
                        + "KhCM8acJE2JHqlEm/tvqARbTBKPUxH3pn9EaXsJ4xNLAqQGWJh97gPswkd2BiadC\n"
                        + "sDICqHdMTaoPZBo0X4FnXr5ou7 bWunprRJhQfkAF55oWGV9As1ozD2kDXrNIJqy\n"
                        + "K9NXLVbmDeKRb85kyAsHGBTqxq6cUmK9MvJy9Xynn rfdD r3TkqF6uj6ptmE/VZ\n"
                        + "Nmhjv3DCe28j102Oj5 aKZtLURjc1qsWEQLzA IdgE08yA68h6bWR2MEPkWVttIA\n"
                        + "F9ZTAl6dN6bfCCnV/8 b5HMfsh/g0U8e1zAp /C8ubWfhq51tvKbm7XKAj0Zl3t0\n"
                        + "pHqBA6NVkpqIhOxsc52vFeXZ3G lAr/8UpJvKhNIwSmPx4mm1xkWwLIcRlUm UCV\n"
                        + "pJeOBd8ShIlEBJxe7EneIwGEHxguE2m/luhn0Q8nRdUHp4NDnHoALqTBgc5MRrXv\n"
                        + "NQW kiZDnNOLQosu0O4/8ihwgNbV a4emvojXy4zD//5gISJVnKGgw0tDeHflbkF\n" + "0q3UGAvnEZ0rSy8=\n"
                        + "=PgL/\n" + "- -----END PGP MESSAGE-----\n" + "\n" + "-----BEGIN PGP SIGNATURE-----\n"
                        + "Version: Subutai Social v1.0.0\n" + "Comment: https://subutai.io/\n" + "\n"
                        + "wsFcBAEBCAAQBQJWbr2dCRCVh5hg8XTaEAAA8cQP/AzhQd1kKx7TNJfkY/vF\n"
                        + "otXslh/QVwg9HqgAq710QdWVlim/1AS1Muzy5tLN2p3TeDwKSwEgnkRu3czL\n"
                        + "2G8ENPRI/nQV1T6NeLhso/oiqdqyttaFpPRgq6pOm0S6CD7hqOS2Brf2ha51\n"
                        + "SqfOWy6UgI2QEfHyKQ8bc307dW6dj4yLc6GEJuHuj4Lyk8cZTXHbmtQwZEqE\n"
                        + "xu36G60SzbBJ88BG tp22IHRYxuCUNeDMh/zrIv/c8EHOmRgrG/AeIu3bmI1\n"
                        + "otEYcbO2nbgpq DXLDKFaDtn2Lak8UJa6v7pZXwhWQYlR//eXPZrmSNG3T N\n"
                        + "UGeyfZHbAu fp2o4pLCrS0kw2hWUJcoHyl3gcoto QAJRudLM7rNBDjWwvFE\n"
                        + "WakfqmT4r2kSAckyWKXvrZibEVz0XrNxuIKBjOBo6VUOCfJu zyHxsr0/fEB\n"
                        + "bAclK3 5eNy7rMNGHoAHBT0gaRb27LhiOlrHaASPzLYzX9iI89pyXRJfcYvm\n"
                        + "YiAvShvqCbHSECPByFC8xu9xkGm2PgvzgLr vl7ZOgJjqu3qOuopI j3l820\n"
                        + "2dO4el8mGYoGLkmB0Q18KCvaqkvlnDC94GQKFaI2YeV/a6JC8BxG0xkm PHf\n"
                        + "w1RevNd8Oge0eCJeQw0aaLwaUbQgdsbY rRyjFQFtWPAcJfxtsRj0pFQRIGM\n" + "ZoSP\n" + "=8OOc\n"
                        + "-----END PGP SIGNATURE-----";
    }
}
