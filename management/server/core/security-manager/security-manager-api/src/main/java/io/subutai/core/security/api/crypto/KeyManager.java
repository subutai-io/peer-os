package io.subutai.core.security.api.crypto;


import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;


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
     * Gets KeyRing from the store and returns Publickey object
     */
    public PGPPublicKey getPublicKey( String hostId );


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
    public PGPSecretKey getSecretKeyById( String keyId );


    /* *****************************
     *
     */
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint );


    /* *****************************
     *
     */
    public void savePublicKeyRing( String hostId, String keyringAsASCII );


    /* *****************************
     *
     */
    public void savePublicKeyRing( String hostId, PGPPublicKeyRing publicKeyRing );


    /* *****************************
     *
     */
    public void removePublicKeyRing( String hostId );


}
