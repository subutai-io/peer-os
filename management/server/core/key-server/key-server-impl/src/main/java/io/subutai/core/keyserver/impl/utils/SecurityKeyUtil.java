package io.subutai.core.keyserver.impl.utils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import org.apache.commons.codec.binary.Hex;

import io.subutai.core.keyserver.api.model.PublicKeyStore;
import io.subutai.core.keyserver.impl.model.PublicKeyStoreEntity;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;

/**
 * Utility Class for SecurityKey
 */
public class SecurityKeyUtil
{

    /********************************************
     *  Convert BouncyCastle PGPKey to SecurityKey entity
     *
     * @param pgpKey
     * @return
     * @throws IOException
     */
    public static PublicKeyStore convert( PGPPublicKey pgpKey ) throws IOException
    {
        String fingerprint = new String( Hex.encodeHex( pgpKey.getFingerprint(), false ) );

        PublicKeyStore pk = new PublicKeyStoreEntity();

        pk.setFingerprint( fingerprint );
        pk.setKeyId( PGPKeyUtil.getKeyId( fingerprint ) );
        pk.setShortKeyId( PGPKeyUtil.getShortKeyId( fingerprint ) );
        pk.setKeyData( pgpKey.getPublicKeyPacket().getEncoded());


        return pk;
    }


    /********************************************
     *  Convert BouncyCastle PGPKeyRing to SecurityKey entity
     *
     * @param pgpKeyRing
     * @return
     * @throws IOException
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
        catch(Exception ex)
        {
            return null;
        }
    }


    /********************************************
     * Returns PGPPublicKey instance that corresponds to the given Security key.
     *
     * @param publicKey
     * @return
     * @throws PGPException
     */
    public static PGPPublicKey convert( PublicKeyStore publicKey ) throws PGPException
    {
        return PGPKeyUtil.readPublicKey( new ByteArrayInputStream( publicKey.getKeyData() ) );
    }


    /********************************************
     * Exports given Security key as ASCII armored text.
     *
     * @param securityKey key to export
     * @return ASCII armored key text
     * @throws PGPException
     */

    public static String exportAscii( PublicKeyStore securityKey ) throws PGPException
    {
        PGPPublicKey pgpKey = convert( securityKey );
        return PGPKeyUtil.exportAscii( pgpKey );
    }

}
