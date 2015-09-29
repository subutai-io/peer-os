package io.subutai.core.security.api.crypto;


import java.io.InputStream;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.security.crypto.pgp.KeyPair;


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


    public String getPeerId();

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
    public void savePublicKeyRing( String hostId, short type, String keyringAsASCII );


    /* *****************************
     *
     */
    public void savePublicKeyRing( String hostId, short type, PGPPublicKeyRing publicKeyRing );


    /* *****************************
     *
     */
    public void saveSecretKeyRing( String hostId, short type, PGPSecretKeyRing publicKeyRing );


    /* *****************************
     *
     */
    public void removePublicKeyRing( String hostId );


    /* *****************************************
     * Removes SecretKeyRing from the Store
     */
    public void removeSecretKeyRing( String hostId );


    /* *****************************************
     *
     */
    public KeyPair generateKeyPair( String userId, boolean armored );


    /* *****************************************
     *
     */
    public void saveKeyPair( String hostId, short type, KeyPair keyPair );


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
}
