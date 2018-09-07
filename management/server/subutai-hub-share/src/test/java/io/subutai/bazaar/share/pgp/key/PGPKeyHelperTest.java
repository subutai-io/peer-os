package io.subutai.bazaar.share.pgp.key;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Test;

import org.apache.commons.io.FileUtils;

import io.subutai.bazaar.share.pgp.common.PGPTestDataFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PGPKeyHelperTest
{
    private static void checkPublicKey( PGPPublicKey publicKey )
    {
        assertNotNull( publicKey );

        assertTrue( PGPKeyHelper.getOwnerString( publicKey ).contains( PGPTestDataFactory.PUBLIC_KEY_OWNER ) );

        assertEquals( PGPTestDataFactory.PUBLIC_KEY_FIGNERPRINT, PGPKeyHelper.getFingerprint( publicKey ) );
    }


    @Test
    public void testReadPublicKeyFromStream() throws IOException, PGPException
    {
        try ( InputStream is = new FileInputStream( PGPTestDataFactory.PUBLIC_KEY_PATH ) )
        {
            PGPPublicKey publicKey = PGPKeyHelper.readPublicKey( is );

            checkPublicKey( publicKey );
        }
    }


    @Test
    public void testReadPublicKeyFromPath() throws IOException, PGPException
    {
        PGPPublicKey publicKey = PGPKeyHelper.readPublicKey( PGPTestDataFactory.PUBLIC_KEY_PATH );

        checkPublicKey( publicKey );
    }


    @Test
    public void testGetPrivateKeyFromStream() throws IOException, PGPException
    {
        try ( InputStream is = new FileInputStream( PGPTestDataFactory.PRIVATE_KEY_PATH ) )
        {
            PGPPrivateKey privateKey = PGPKeyHelper.readPrivateKey( is, PGPTestDataFactory.DEFAULT_PASSWORD );

            assertNotNull( privateKey );
        }
    }


    @Test
    public void testGetPrivateKeyFromPath() throws IOException, PGPException
    {
        PGPPrivateKey privateKey = PGPKeyHelper.readPrivateKey( PGPTestDataFactory.PRIVATE_KEY_PATH, PGPTestDataFactory.DEFAULT_PASSWORD );

        assertNotNull( privateKey );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testReadPublicKeyFromString_emptyString() throws IOException, PGPException
    {
        PGPKeyHelper.readPublicKeyFromString( "" );
    }


    @Test
    public void testReadPublicKeyFromString_success() throws IOException, PGPException
    {
        String str = FileUtils.readFileToString( new File( PGPTestDataFactory.PUBLIC_KEY_PATH ) );

        PGPPublicKey publicKey = PGPKeyHelper.readPublicKeyFromString( str );

        assertNotNull( publicKey );
    }
}
