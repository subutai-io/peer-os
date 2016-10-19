package io.subutai.common.security.crypto.pgp;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;


public class PGPKeyUtilTest
{

    PGPPublicKey pgpPublicKey;
    String fingerprint;
    String longKeyId;
    String shortKeyId;


    @Before
    public void setUp() throws Exception
    {
        pgpPublicKey = PGPEncryptionUtil
                .findPublicKeyById( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.PUBLIC_KEYRING ),
                        PGPEncryptionUtilTest.PUBLIC_KEY_ID );

        fingerprint = PGPKeyUtil.getFingerprint( pgpPublicKey.getFingerprint() );

        longKeyId = PGPKeyUtil.getKeyId( fingerprint );

        shortKeyId = PGPKeyUtil.getShortKeyId( fingerprint );
    }


    @Test
    public void testEncodeNumericKeyId() throws Exception
    {
        assertEquals( PGPKeyUtil.encodeNumericKeyId( 123 ).length(), 16 );
    }


    @Test
    public void testGetKeyId() throws Exception
    {

        assertEquals( ( PGPKeyUtil.getKeyId( new String( fingerprint ) ).length() ), 16 );
    }


    @Test
    public void testGetShortKeyId() throws Exception
    {

        assertEquals( PGPKeyUtil.getShortKeyId( fingerprint ).length(), 8 );
    }


    @Test
    public void testIsFingerprint() throws Exception
    {
        assertTrue( PGPKeyUtil.isFingerprint( fingerprint ) );

        assertFalse( PGPKeyUtil.isFingerprint( "blablabla" ) );
    }


    @Test
    public void testIsLongKeyId() throws Exception
    {
        assertTrue( PGPKeyUtil.isLongKeyId( longKeyId ) );

        assertFalse( PGPKeyUtil.isLongKeyId( shortKeyId ) );
    }


    @Test
    public void testIsShortKeyId() throws Exception
    {
        assertTrue( PGPKeyUtil.isShortKeyId( shortKeyId ) );

        assertFalse( PGPKeyUtil.isShortKeyId( longKeyId ) );
    }


    @Test
    public void testIsValidKey() throws Exception
    {
        assertTrue( PGPKeyUtil.isValidKeyId( longKeyId ) );

        assertFalse( PGPKeyUtil.isValidKeyId( "blablabla" ) );
    }


    @Test
    public void testExportAscii() throws Exception
    {
        assertThat( PGPKeyUtil.exportAscii( pgpPublicKey ), startsWith( "-----BEGIN PGP MESSAGE-----" ) );
    }


    @Test
    public void testReadPublicKey() throws Exception
    {

        assertNotNull(
                PGPKeyUtil.readPublicKey( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.PUBLIC_KEYRING ) ) );
    }


    @Test
    public void testReadPublicKey2() throws Exception
    {

        assertNotNull( PGPKeyUtil.readPublicKey( PGPEncryptionUtil.armorByteArrayToString(
                IOUtils.toByteArray( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.PUBLIC_KEYRING ) ) ) ) );
    }


    @Test
    public void testReadPublicKey3() throws Exception
    {

        assertNotNull( PGPKeyUtil.readPublicKey( PGPKeyUtil
                .readPublicKeyRing( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.PUBLIC_KEYRING ) ) ) );
    }


    @Test
    public void testReadPublicKeyRing() throws Exception
    {
        assertNotNull( PGPKeyUtil
                .readPublicKeyRing( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.PUBLIC_KEYRING ) ) );
    }


    @Test
    public void testReadPublicKeyring2() throws Exception
    {
        assertNotNull( PGPKeyUtil.readPublicKeyRing(
                IOUtils.toByteArray( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.PUBLIC_KEYRING ) ) ) );
    }


    @Test
    public void testReadPublicKeyring3() throws Exception
    {
        assertNotNull( PGPKeyUtil.readPublicKeyRing( PGPKeyUtil.exportAscii( pgpPublicKey ) ) );
    }


    @Test
    public void testIsValidPublicKeyRing() throws Exception
    {

        assertTrue( PGPKeyUtil.isValidPublicKeyring( PGPKeyUtil.exportAscii( pgpPublicKey ) ) );

        assertFalse( PGPKeyUtil.isValidPublicKeyring( "blablabla" ) );
    }


    @Test
    public void testReadSecretKeyRing() throws Exception
    {

        assertNotNull( PGPKeyUtil
                .readSecretKeyRing( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.SECRET_KEYRING ) ) );
    }


    @Test
    public void testReadSecretKeyRing2() throws Exception
    {

        assertNotNull( PGPKeyUtil.readSecretKeyRing(
                IOUtils.toByteArray( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.SECRET_KEYRING ) ) ) );
    }


    @Test
    public void testReadSecretKey() throws Exception
    {
        PGPSecretKeyRing secretKeyRing =
                PGPKeyUtil.readSecretKeyRing( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.SECRET_KEYRING ) );

        assertNotNull( PGPKeyUtil.readSecretKey( secretKeyRing ) );
    }


    @Test
    public void testReadSecretKeyRingInputStream() throws Exception
    {
        assertNotNull( PGPKeyUtil.readSecretKeyRingInputStream(
                IOUtils.toByteArray( PGPEncryptionUtilTest.findFile( PGPEncryptionUtilTest.SECRET_KEYRING ) ) ) );
    }
}
