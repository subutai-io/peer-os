package io.subutai.core.security.api.crypto;


import java.io.InputStream;
import java.util.List;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.api.model.SecurityKeyTrust;


/**
 * Interface for KeyManager
 */
public interface KeyManager
{
    /* *****************************
     *
     */
    public String getPublicKeyRingAsASCII( String hostId );


    /* *****************************
     * Gets KeyRing from the store
     */
    public PGPPublicKeyRing getPublicKeyRing( String hostId );


    /* *****************************
     * Gets SecretKeyRing from the store
     */
    public PGPSecretKeyRing getSecretKeyRing( String hostId );


    /* *****************************
     * Gets KeyRing from the store and returns Publickey object
     */
    public PGPPublicKey getPublicKey( String hostId );


    /* *****************************
     *
     */
    public String getPeerId();


    /* *****************************
     *
     */
    public String getOwnerId();


    /* *****************************
     *
     */
    public PGPSecretKey getSecretKey( String hostId );


    /* *****************************
     *
     */
    public PGPPrivateKey getPrivateKey( String hostId );


    /* *****************************
     *
     */
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint );


    /* *****************************
     *
     */
    public void savePublicKeyRing( String hostId, int type, String keyringAsASCII );


    /* *****************************
     *
     */
    public void savePublicKeyRing( String hostId, int type, PGPPublicKeyRing publicKeyRing );



    /* ***************************************************************
     *
     */
    void setKeyTrust( String sourceId, String targetId, int trustLevel );


    /* ***************************************************************
     *
     */
    SecurityKeyTrust getKeyTrust( String sourceId, String targetId );


    /* ***************************************************************
     *
     */
    List<SecurityKeyTrust> getKeyTrust( String sourceId );


    /* ***************************************************************
     *
     */
    void removeKeyTrust( String sourceId );

    /* ***************************************************************
     *
     */
    void removeKeyTrust( String sourceId, String targetId );

    /* *****************************
     *
     */
    public void saveSecretKeyRing( String hostId, int type, PGPSecretKeyRing publicKeyRing );


    /* *****************************
     *
     */
    public void removePublicKeyRing( String hostId );


    /* *****************************************
     * Removes SecretKeyRing from the Store
     */
    public void removeSecretKeyRing( String hostId );


    /* *****************************************
     * Retrieves host key identity data
     */
    public SecurityKeyIdentity getKeyIdentityData( String hostId );


    /* *****************************************
     *
     */
    public KeyPair generateKeyPair( String userId, boolean armored );


    /* *****************************************
     *
     */
    public void saveKeyPair( String hostId, int type, KeyPair keyPair );


    /* *****************************************
     * Removes Secret and PublicKeyrings from the Store
     */
    public void removeKeyRings( String hostId );


    /* *****************************
     *
     */
    public InputStream getSecretKeyRingInputStream( String hostId );


    /* *****************************
     *
     */
    public PGPPublicKey getRemoteHostPublicKey( String hostId, String ip );


    /* *****************************
     *
     */
    public String getFingerprint( String hostId );


    SecurityKeyIdentity getKeyTrustTree( String hostId );

    KeyTrustLevel getTrustLevel( String aHost, String bHost );
}
