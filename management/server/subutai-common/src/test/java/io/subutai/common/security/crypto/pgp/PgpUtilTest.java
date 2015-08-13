package io.subutai.common.security.crypto.pgp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


public class PgpUtilTest
{
    private static final String MESSAGE = "hello";
    private static final String PUBLIC_KEYRING = "dummy.pkr";
    private static final String SECRET_KEYRING = "dummy.skr";

    private static final String SECRET_PWD = "12345678";
    private static final String PUBLIC_KEY_ID = "e2451337c277dbf1";
    private static final String SECRET_KEY_ID = "d558f9a4a0b450b3";
    private static final String USER_ID = "user@mail.org";
    private static final String PUBLIC_KEY_FINGERPRINT = "8338133EF14DE47D4B1646BEE2451337C277DBF1";
    private static final String SECRET_KEY_FINGERPRINT = "3E5DB4DCF15A31C93CF3C9D8D558F9A4A0B450B3";


    public static InputStream findFile( final String file )
    {
        return PgpUtilTest.class.getClassLoader().getResourceAsStream( file );
    }


    @Test
    public void testSignVerify() throws Exception
    {
        PGPSecretKey secretKey = PgpUtil.findSecretKeyById( findFile( SECRET_KEYRING ), SECRET_KEY_ID );
        byte[] signedMessage = PgpUtil.sign( MESSAGE.getBytes(), secretKey, SECRET_PWD, true );

        assertTrue( PgpUtil.verify( signedMessage, secretKey.getPublicKey() ) );
    }


    @Test
    public void testEncryptAndDecrypt() throws Exception
    {
        byte[] encryptedMessage = PgpUtil.encrypt( MESSAGE.getBytes(),
                PgpUtil.findPublicKeyById( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_ID ), true );

        byte[] decryptedMessage = PgpUtil.decrypt( encryptedMessage, findFile( SECRET_KEYRING ), SECRET_PWD );

        assertTrue( Arrays.equals( MESSAGE.getBytes(), decryptedMessage ) );
    }


    @Test
    public void testSignEncryptAndDecryptVerify() throws Exception
    {
        PGPSecretKey signingKey =
                PgpUtil.findSecretKeyByFingerprint( findFile( SECRET_KEYRING ), SECRET_KEY_FINGERPRINT );
        PGPPublicKey encryptingKey =
                PgpUtil.findPublicKeyByFingerprint( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_FINGERPRINT );

        byte[] signedAndEncryptedMessage =
                PgpUtil.signAndEncrypt( MESSAGE.getBytes(), signingKey, SECRET_PWD, encryptingKey, true );

        PGPSecretKey decryptingSecretKey = PgpUtil.findSecretKeyByFingerprint( findFile( SECRET_KEYRING ),
                PgpUtil.BytesToHex( encryptingKey.getFingerprint() ) );

        byte[] decryptedAndVerifiedMessage =
                PgpUtil.decryptAndVerify( signedAndEncryptedMessage, decryptingSecretKey, SECRET_PWD,
                        signingKey.getPublicKey() );

        assertTrue( Arrays.equals( MESSAGE.getBytes(), decryptedAndVerifiedMessage ) );

        //auto secret key detection
        decryptedAndVerifiedMessage =
                PgpUtil.decryptAndVerify( signedAndEncryptedMessage, findFile( SECRET_KEYRING ), SECRET_PWD,
                        signingKey.getPublicKey() );

        assertTrue( Arrays.equals( MESSAGE.getBytes(), decryptedAndVerifiedMessage ) );
    }


    @Test
    public void testGenerateKeyPair() throws Exception
    {
        ByteArrayOutputStream publicKeyRing = new ByteArrayOutputStream();
        ByteArrayOutputStream secretKeyRing = new ByteArrayOutputStream();
        PgpUtil.KeyRef keyRef = PgpUtil.generateKeyPair( USER_ID, SECRET_PWD, publicKeyRing, secretKeyRing );

        assertNotNull( PgpUtil.findPublicKeyById( new ByteArrayInputStream( publicKeyRing.toByteArray() ),
                keyRef.getEncryptingKeyId() ) );
        assertNotNull( PgpUtil.findSecretKeyByFingerprint( new ByteArrayInputStream( secretKeyRing.toByteArray() ),
                keyRef.getSigningKeyFingerprint() ) );
    }


    @Test
    public void testGetX509CertificateFromPgpKeyPair() throws Exception
    {

        Date today = new Date();
        PGPPublicKey pgpPublicKey = PgpUtil.findPublicKeyById( findFile( PUBLIC_KEYRING ), PUBLIC_KEY_ID );
        PGPSecretKey pgpSecretKey = PgpUtil.findSecretKeyById( findFile( SECRET_KEYRING ), SECRET_KEY_ID );
        X509Certificate x509Certificate =
                PgpUtil.getX509CertificateFromPgpKeyPair( pgpPublicKey, pgpSecretKey, SECRET_PWD,
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
}
