package io.subutai.core.keyserver.impl.utils;


import java.io.IOException;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import org.apache.commons.codec.binary.Hex;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.keyserver.api.model.PublicKeyStore;
import io.subutai.core.keyserver.impl.model.PublicKeyStoreEntity;


/**
 * Utility Class for SecurityKey
 */
public class SecurityKeyUtil
{
    private SecurityKeyUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    /********************************************
     * Convert BouncyCastle PGPKey to SecurityKey entity
     */
    public static PublicKeyStore convert( PGPPublicKey pgpKey ) throws IOException
    {
        String fingerprint = new String( Hex.encodeHex( pgpKey.getFingerprint(), false ) );

        PublicKeyStore pk = new PublicKeyStoreEntity();

        pk.setFingerprint( fingerprint );
        pk.setKeyId( PGPKeyUtil.getKeyId( fingerprint ) );
        pk.setShortKeyId( PGPKeyUtil.getShortKeyId( fingerprint ) );
        pk.setKeyData( pgpKey.getPublicKeyPacket().getEncoded() );


        return pk;
    }


    /********************************************
     * Convert BouncyCastle PGPKeyRing to SecurityKey entity
     */
    public static PublicKeyStore convert( PGPPublicKeyRing pgpKeyRing ) throws IOException
    {
        try
        {
            PGPPublicKey pgpKey = PGPKeyUtil.readPublicKey( pgpKeyRing );

            if ( pgpKey != null )
            {
                String fingerprint = new String( Hex.encodeHex( pgpKey.getFingerprint(), false ) );

                PublicKeyStore pk = new PublicKeyStoreEntity();

                pk.setFingerprint( fingerprint );
                pk.setKeyId( PGPKeyUtil.getKeyId( fingerprint ) );
                pk.setShortKeyId( PGPKeyUtil.getShortKeyId( fingerprint ) );
                pk.setKeyData( pgpKeyRing.getEncoded() );

                return pk;
            }
            else
            {
                return null;
            }
        }
        catch ( Exception ex )
        {
            return null;
        }
    }
}
