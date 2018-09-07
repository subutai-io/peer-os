package io.subutai.bazaar.share.pgp.key;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Test;

import io.subutai.bazaar.share.pgp.common.PGPTestDataFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class PGPSignatureUtilTest
{
    @Test
    public void testIsSignedBy() throws Exception
    {
        PGPPublicKey applePublicKey = PGPTestDataFactory.getPublicKey( "apple" );

        PGPPublicKey alicePublicKey = PGPTestDataFactory.getPublicKey( "alice" );

        assertTrue( PGPSignatureUtil.isSignedBy( applePublicKey, alicePublicKey ) );

        PGPPublicKey bobbyPublicKey = PGPTestDataFactory.getPublicKey( "bobby" );

        assertFalse( PGPSignatureUtil.isSignedBy( applePublicKey, bobbyPublicKey ) );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testMergeSignatures_withDifferentPubKeys() throws Exception
    {
        PGPPublicKey alicePuKey = PGPTestDataFactory.getPublicKey( "alice" );

        PGPPublicKey applePubKey = PGPTestDataFactory.getPublicKey( "apple" );

        PGPSignatureUtil.mergeSignatures( alicePuKey, applePubKey );
    }


    @Test
    public void testMergeSignatures_success() throws Exception
    {
        PGPPublicKey applePubKey = PGPTestDataFactory.getPublicKey( "apple" );
        PGPPublicKey appleCleanKey = PGPTestDataFactory.getPublicKey( "apple-without-trust-sigs" );

        assertEquals( 2, PGPSignatureUtil.getSignatures( applePubKey ).size() );
        assertEquals( 1, PGPSignatureUtil.getSignatures( appleCleanKey ).size() );

        PGPPublicKey newPubKey = PGPSignatureUtil.mergeSignatures( appleCleanKey, applePubKey );

        assertEquals( 2, PGPSignatureUtil.getSignatures( newPubKey ).size() );
    }
}
