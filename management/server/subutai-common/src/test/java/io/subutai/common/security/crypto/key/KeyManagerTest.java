package io.subutai.common.security.crypto.key;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;


@Ignore
public class KeyManagerTest
{
    private KeyManager keyManager;
    private KeyPairGenerator keyPairGenerator;
    private KeyPairGenerator keyPairGenerator2;
    private KeyPair keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey secretKey;
    private Signature signature;

    @Mock
    KeyGenerator keyGenerator;


    @Before
    public void setUp() throws Exception
    {
        keyManager = new KeyManager();
        keyPairGenerator = keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
        keyPairGenerator2 = keyManager.prepareKeyPairGeneration( KeyPairType.DSA, 5 );
        keyPair = keyManager.generateKeyPair( keyPairGenerator );
        privateKey = keyManager.generatePrivateKeyPair( keyPair );
        publicKey = keyManager.generatePublicKeyPair( keyPair );
        secretKey = keyManager.generateSecretKey( keyGenerator );

        keyManager.setKeyGen( keyPairGenerator );
        keyManager.setKeyGen( keyGenerator );
        keyManager.setKeypair( keyPair );
        keyManager.setKeyPairGen( keyPairGenerator );
        keyManager.setKeypair( keyPair );
        keyManager.setPrivateKey( privateKey );
        keyManager.setPublicKey( publicKey );
        keyManager.setKeyPair( keyPair );
        keyManager.setSecretKey( secretKey );
        keyManager.setSignature( signature );
    }


    @Test
    public void testProperties()
    {
        assertNotNull( keyManager.getKeyGen() );
        assertNotNull( keyManager.getKeypair() );
        assertNotNull( keyManager.getKeyPair() );
        assertNotNull( keyManager.getPrivateKey() );
        assertNotNull( keyManager.getPublicKey() );
        assertNotNull( keyManager.getKeyPairGen() );
        assertNotNull( keyManager.getKeyPair() );
        keyManager.getSecretKey();
        keyManager.getSignature();
    }


    @Test
    public void testPrepareKeyPairGeneration() throws Exception
    {
        assertNotNull( keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 1024 ) );
    }


    @Test
    public void testPrepareKeyPairGenerationException() throws Exception
    {
        keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 5 );
    }


    @Test
    public void testGenerateKeyPair() throws Exception
    {
        assertNotNull( keyManager.generateKeyPair( keyPairGenerator ) );
        keyManager.generateKeyPair( keyPairGenerator2 );
    }


    @Test
    public void testGeneratePrivateKeyPair() throws Exception
    {
        assertNotNull( keyManager.generatePrivateKeyPair( keyPair ) );
    }


    @Test
    public void testGeneratePublicKeyPair() throws Exception
    {
        assertNotNull( keyManager.generatePublicKeyPair( keyPair ) );
    }


    @Test
    public void testGeneratePrivateKeyPairBytes() throws Exception
    {
        assertNotNull( keyManager.generatePrivateKeyPairBytes( keyPair ) );
    }


    @Test
    public void testGeneratePublicKeyPairBytes() throws Exception
    {
        assertNotNull( keyManager.generatePublicKeyPairBytes( keyPair ) );
    }


    @Test
    public void testPrepareSecretKeyGeneration() throws Exception
    {
        keyManager.prepareSecretKeyGeneration( SecretKeyType.AES );
    }


    @Test
    public void testGenerateSecretKey() throws Exception
    {
        keyManager.generateSecretKey( keyGenerator );
    }


    @Test
    public void testSignData() throws Exception
    {
        keyManager.signData( SignatureType.SHA1_DSA, keyPair, "test" );
    }


    @Test
    public void testVerifySignedData() throws Exception
    {
        byte[] sign = new byte[5];

        keyManager.verifySignedData( SignatureType.SHA1_DSA, keyPair, "test", sign );
    }
}