package io.subutai.bazaar.share.pgp.crypto;


import org.bouncycastle.openpgp.PGPDataValidationException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Test;

import io.subutai.bazaar.share.pgp.common.PGPTestDataFactory;

import static org.junit.Assert.assertArrayEquals;


public class PGPSignVerifyTest
{
    @Test
    public void testSuccess() throws Exception
    {
        byte data[] = PGPTestDataFactory.getData();

        PGPPrivateKey privateKey = PGPTestDataFactory.getPrivateKey( "alice" );

        PGPPublicKey publicKey = PGPTestDataFactory.getPublicKey( "alice" );

        // Testing more than 10 times to be sure b/c there may be failures
        for ( int i = 1; i <= 20; i++ )
        {
            test( data, privateKey, publicKey );
        }
    }


    private static void test( byte data[], PGPPrivateKey privateKey, PGPPublicKey publicKey ) throws Exception
    {
        byte signedData[] = PGPSign.sign( data, privateKey );

        byte outData[] = PGPVerify.verify( signedData, publicKey );

        assertArrayEquals( data, outData );
    }


    @Test( expected = PGPDataValidationException.class )
    public void testFail() throws Exception
    {
        PGPPrivateKey privateKey = PGPTestDataFactory.getPrivateKey( "alice" );

        // Give wrong key for validation
        PGPPublicKey publicKey = PGPTestDataFactory.getPublicKey( "bobby" );

        test( PGPTestDataFactory.getData(), privateKey, publicKey );
    }
}
