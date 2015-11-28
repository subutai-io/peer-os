package io.subutai.core.security.api.crypto;


import java.io.InputStream;
import java.util.List;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.security.crypto.pgp.KeyPair;
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
    PGPPublicKeyRing getPublicKeyRingByFingerprint( String fingerprint );

    /* *****************************
     *
     */
    public String getPublicKeyRingAsASCII( String identityId );


    /* *****************************
     * Gets KeyRing from the store
     */
    public PGPPublicKeyRing getPublicKeyRing( String identityId );


    /* *****************************
     * Gets SecretKeyRing from the store
     */
    public PGPSecretKeyRing getSecretKeyRing( String identityId );


    /* *****************************
     *
     */
    SecurityKeyIdentity getKeyIdentityDataByFingerprint( String fingerprint );

    /* *****************************
     *
     */
    void removeKeyIdentityData( String identityId );

    /* *****************************
     * Gets KeyRing from the store and returns Publickey object
     */
    public PGPPublicKey getPublicKey( String identityId );


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
    PGPSecretKey getSecretKey( String identityId );


    /* *****************************
     *
     */
    PGPPrivateKey getPrivateKey( String identityId );


    /* *****************************
     *
     */
    PGPSecretKey getSecretKeyByFingerprint( String fingerprint );


    /* *****************************
     *
     */
    void savePublicKeyRing( String identityId, int type, String keyringAsASCII );


    /* *****************************
     *
     */
    void savePublicKeyRing( String identityId, int type, PGPPublicKeyRing publicKeyRing );


    /* ***************************************************************
     *
     */
    PGPPublicKeyRing signKey( PGPSecretKeyRing sourceSecRing, PGPPublicKeyRing targetPubRing, int trustLevel );


    /* ***************************************************************
     *
     */
    PGPPublicKeyRing signKey( String sourceIdentityId, String targetIdentityId, int trustLevel );


    /* ***************************************************************
     *
     */
    String signPublicKey( String sourceIdentityId, String keyText, int trustLevel );


    /* ***************************************************************
     *
     */
    void setKeyTrust( String sourceFingerprint, String targetFingerprint, int trustLevel );


    /* ***************************************************************
     *
     */
    PGPPublicKeyRing removeSignature( String sourceFingerprint, String targetFingerprint );

    /* ***************************************************************
         *
         */
    PGPPublicKeyRing removeSignature( String sourceIdentityId, PGPPublicKeyRing targetPubRing );


    /* ***************************************************************
     *
     */
    SecurityKeyTrust getKeyTrustData(String sourceFingerprint, String targetFingerprint);


    /* ***************************************************************
     *
     */
    List<SecurityKeyTrust> getKeyTrustData( String sourceFingerprint );


    /* ***************************************************************
     *
     */
    void removeKeyTrust( String sourceFingerprint );


    /* ***************************************************************
     *
     */
    void removeKeyTrust( String sourceFingerprint, String targetFingerprint);


    /* *****************************
     *
     */
     void saveSecretKeyRing( String hostId, int type, PGPSecretKeyRing publicKeyRing );


    /* *****************************
     *
     */
    void removePublicKeyRing( String identityId );


    /* *****************************************
     * Removes SecretKeyRing from the Store
     */
    void removeSecretKeyRing( String identityId );



    /* *****************************************
     * Retrieves host key identity data
     */
    SecurityKeyIdentity getKeyIdentityData( String identityId );


    /* *****************************************
     *
     */
    KeyPair generateKeyPair( String identityId, boolean armored );


    /* *****************************************
     *
     */
    public void saveKeyPair( String identityId, int type, KeyPair keyPair );


    /* *****************************************
     * Removes Secret and PublicKeyrings from the Store
     */
    public void removeKeyRings( String identityId );


    /* *****************************
     *
     */
    public InputStream getSecretKeyRingInputStream( String identityId );



    /* *****************************
     *
     */
    public PGPPublicKey getRemoteHostPublicKey( String identityId, String ip );



    /* *****************************
     *
     */
    public String getFingerprint( String identityId );


    /* *****************************
     *
     */
    SecurityKeyIdentity getKeyTrustTree( String identityId );


    /* *****************************
     *
     */
    int getTrustLevel(final String sourceIdentityId, final String targetIdentityId );


    /* *****************************
     *
     */
    void updatePublicKeyRing( PGPPublicKeyRing publicKeyRing );
}
