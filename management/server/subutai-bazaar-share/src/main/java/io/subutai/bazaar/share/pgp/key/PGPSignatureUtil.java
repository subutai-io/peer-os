package io.subutai.bazaar.share.pgp.key;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;


/**
 * Utility for PGP signature related operations.
 */
public class PGPSignatureUtil
{

    private PGPSignatureUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    /**
     * Checks if publicKey is signed by signerPublicKey, without any verification. In future we have to review carefully
     * the PGP Specs and implement the verification. See also: http://osdir.com/ml/encryption.bouncy-castle
     * .devel/2006-12/msg00005.html
     */
    public static boolean isSignedBy( PGPPublicKey pubKey, PGPPublicKey signerPubKey )
    {
        PGPSignature sig = getSignatures( pubKey ).get( signerPubKey.getKeyID() );

        return sig != null && ( sig.getSignatureType() == PGPSignature.DEFAULT_CERTIFICATION
                || sig.getSignatureType() == PGPSignature.DIRECT_KEY );
    }


    /**
     * Returns a map containing key IDs and corresponding signatures.
     */
    public static Map<Long, PGPSignature> getSignatures( PGPPublicKey pubKey )
    {
        HashMap<Long, PGPSignature> sigs = new HashMap<>();
        Iterator signatures = pubKey.getSignatures();

        while ( signatures.hasNext() )
        {
            PGPSignature sign = ( PGPSignature ) signatures.next();

            sigs.put( sign.getKeyID(), sign );
        }

        return sigs;
    }


    /**
     * Returns a public key containing signatures of two keys.
     */
    public static PGPPublicKey mergeSignatures( PGPPublicKey targetKey, PGPPublicKey sourceKey ) throws PGPException
    {
        if ( !Objects.deepEquals( targetKey.getFingerprint(), sourceKey.getFingerprint() ) )
        {
            throw new IllegalArgumentException(
                    "Signature merge can be done for different instances of the same public key only" );
        }

        return copySignatures( targetKey, sourceKey );
    }


    private static PGPPublicKey copySignatures( PGPPublicKey targetKey, PGPPublicKey sourceKey )
    {
        Map<Long, PGPSignature> targetSigs = getSignatures( targetKey );
        Map<Long, PGPSignature> sourceSigs = getSignatures( sourceKey );

        for ( Map.Entry<Long, PGPSignature> e : sourceSigs.entrySet() )
        {
            if ( !targetSigs.containsKey( e.getKey() ) )
            {
                targetKey = PGPPublicKey.addCertification( targetKey, e.getValue() );
            }
        }

        return targetKey;
    }
}
