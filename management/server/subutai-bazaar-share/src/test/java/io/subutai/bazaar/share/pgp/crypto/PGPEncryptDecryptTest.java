package io.subutai.bazaar.share.pgp.crypto;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Test;

import io.subutai.bazaar.share.pgp.common.PGPTestDataFactory;

import static org.junit.Assert.assertArrayEquals;


public class PGPEncryptDecryptTest
{
    @Test
    public void testSuccess() throws Exception
    {
        byte data[] = PGPTestDataFactory.getData();

        PGPPublicKey publicKey = PGPTestDataFactory.getPublicKey( "alice" );

        PGPPrivateKey privateKey = PGPTestDataFactory.getPrivateKey( "alice" );

        // Testing more than 10 times to be sure b/c there may be failures
        for ( int i = 1; i <= 20; i++ )
        {
            test( data, privateKey, publicKey );
        }
    }


    private static void test( byte data[], PGPPrivateKey privateKey, PGPPublicKey publicKey ) throws Exception
    {
        byte encData[] = PGPEncrypt.encrypt( data, publicKey );

        byte decData[] = PGPDecrypt.decrypt( encData, privateKey );

        assertArrayEquals( data, decData );
    }


    @Test( expected = PGPException.class )
    public void testFail() throws Exception
    {
        PGPPublicKey publicKey = PGPTestDataFactory.getPublicKey( "bobby" );

        PGPPrivateKey privateKey = PGPTestDataFactory.getPrivateKey( "alice" );

        test( PGPTestDataFactory.getData(), privateKey, publicKey );
    }
}
