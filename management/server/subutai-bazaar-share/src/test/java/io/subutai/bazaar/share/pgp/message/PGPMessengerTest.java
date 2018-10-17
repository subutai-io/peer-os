package io.subutai.bazaar.share.pgp.message;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.BeforeClass;
import org.junit.Test;

import io.subutai.bazaar.share.pgp.common.PGPTestDataFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;


public class PGPMessengerTest
{
    private static PGPPrivateKey bobbyPrivateKey;

    private static PGPMessenger aliceToBobbyMessenger;

    private static PGPMessenger bobbyToAliceMessenger;


    @BeforeClass
    public static void setup() throws Exception
    {
        PGPPrivateKey alicePrivateKey = PGPTestDataFactory.getPrivateKey( "alice" );

        PGPPublicKey alicePublicKey = PGPTestDataFactory.getPublicKey( "alice" );

        bobbyPrivateKey = PGPTestDataFactory.getPrivateKey( "bobby" );

        PGPPublicKey bobbyPublicKey = PGPTestDataFactory.getPublicKey( "bobby" );

        aliceToBobbyMessenger = new PGPMessenger( alicePrivateKey, bobbyPublicKey );

        bobbyToAliceMessenger = new PGPMessenger( bobbyPrivateKey, alicePublicKey );
    }


    @Test
    public void testSuccess() throws Exception
    {
        byte data[] = PGPTestDataFactory.getData();

        byte encData[] = aliceToBobbyMessenger.produce( data );

        byte decData[] = bobbyToAliceMessenger.consume( encData );

        assertArrayEquals( data, decData );
    }


    @Test
    public void testNull() throws PGPException
    {
        assertTrue( aliceToBobbyMessenger.produce( null ).length == 0 );

        assertTrue( aliceToBobbyMessenger.consume( null ).length == 0 );
    }


    @Test( expected = PGPException.class )
    public void testFail() throws Exception
    {
        // Alice sends a message to Bobby. Bobby decrypts but verifies the signature with Cindy's public key.

        byte[] encData = aliceToBobbyMessenger.produce( PGPTestDataFactory.getData() );

        PGPPublicKey cindyPublicKey = PGPTestDataFactory.getPublicKey( "cindy" );

        PGPMessenger cindyToBobbyMessenger = new PGPMessenger( bobbyPrivateKey, cindyPublicKey );

        cindyToBobbyMessenger.consume( encData );
    }
}
