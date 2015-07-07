package io.subutai.common.security.crypto.key;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.security.crypto.file.CryptoFileType;
import io.subutai.common.security.crypto.key.DigestType;
import io.subutai.common.security.crypto.key.EncryptionType;
import io.subutai.common.security.crypto.key.KeyInfo;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.crypto.key.KeyType;
import io.subutai.common.security.crypto.key.SecretKeyType;
import io.subutai.common.security.crypto.key.SignatureType;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MockitoJUnitRunner.class )
public class KeyInfoTest
{
    private KeyInfo keyInfo;


    @Before
    public void setUp() throws Exception
    {
        keyInfo = new KeyInfo( KeyType.ASYMMETRIC, "best" );
        keyInfo = new KeyInfo( KeyType.ASYMMETRIC, "best", 5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( keyInfo.getAlgorithm() );
        assertNotNull( keyInfo.getKeyType() );
        assertNotNull( keyInfo.getSize() );
    }


    @Test
    public void testEnumDigestType()
    {
        DigestType md2 = DigestType.MD2;
        assertNotNull( md2.jce() );
        assertNotNull( md2.oid() );
        assertNotNull( md2.friendly() );
        assertNotNull( md2.toString() );
        assertNotNull( DigestType.resolveJce( "MD2" ) );
        assertNull( DigestType.resolveJce( "jce" ) );
    }


    @Test
    public void testEnumEncryptionType()
    {
        EncryptionType encrypted = EncryptionType.ENCRYPTED;
    }


    @Test
    public void testEnumKeyPairType()
    {
        KeyPairType dsa = KeyPairType.DSA;
        assertNotNull( dsa.jce() );
        assertNotNull( dsa.oid() );
        assertNotNull( dsa.maxSize() );
        assertNotNull( dsa.minSize() );
        assertNotNull( dsa.stepSize() );
        assertNotNull( dsa.toString() );
        assertNotNull( dsa.resolveJce( "DSA" ) );
        assertNull( dsa.resolveJce( "jce" ) );
    }


    @Test
    public void testEnumSecretKeyType()
    {
        SecretKeyType aes = SecretKeyType.AES;
        assertNotNull( aes.jce() );
        assertNotNull( aes.maxSize() );
        assertNotNull( aes.minSize() );
        assertNotNull( aes.stepSize() );
        assertNotNull( aes.toString() );
        assertNotNull( aes.resolveJce( "AES" ) );
        assertNull( aes.resolveJce( "jce" ) );
    }


    @Test
    public void testEnumSignatureType()
    {
        SignatureType md5Rsa = SignatureType.MD5_RSA;
        assertNotNull( md5Rsa.jce() );
        assertNotNull( md5Rsa.toString() );
        assertNotNull( md5Rsa.oid() );
        assertNotNull( md5Rsa.friendly() );
        assertNotNull( md5Rsa.digestType() );
        assertNotNull( md5Rsa.dsaSignatureTypes() );
        assertNotNull( md5Rsa.ecdsaSignatureTypes() );
        assertNotNull( md5Rsa.rsaSignatureTypes() );
        assertNotNull( md5Rsa.rsaSignatureTypes( 5 ) );
        assertNotNull( md5Rsa.resolveJce( "SHA1withDSA" ) );
        assertNull( md5Rsa.resolveJce( "jce" ) );
        assertNotNull( md5Rsa.resolveOid( "1.2.840.10040.4.3" ) );
        assertNull( md5Rsa.resolveOid( "jce" ) );
    }


    @Test
    public void testEnumCryptoFileType()
    {
        CryptoFileType bksKs = CryptoFileType.BKS_KS;
        assertNotNull( bksKs.friendly() );
    }
}