package io.subutai.core.security.api.crypto;


import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;


/**
 * Interface for KeyManager
 */
public interface KeyManager
{
    /********************************
     *
     */
    public String getPublicKeyAsASCII(String hostId);


    /********************************
     *
     */
    public PGPPublicKey getPublicKey(String hostId);


    /********************************
     *
     */
    public PGPSecretKey getSecretKey(String hostId);


    /********************************
     *
     */
    public PGPPrivateKey getPrivateKey( String hostId );


    /********************************
     *
     */
    public PGPSecretKey getSecretKeyById( String keyId );


    /********************************
     *
     */
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint );


    /********************************
     *
     */
    public void savePublicKey( String hostId, String keyAsASCII);


    /********************************
     *
     */
    public void savePublicKey( String hostId, PGPPublicKey publicKey);


    /********************************
     *
     */
    public void removePublicKey( String hostId);


    /********************************
     *
     */
    public String getSecretKeyring();


    /********************************
     *
     */
    public void setSecretKeyring( final String secretKeyring );

}
