package io.subutai.core.security.api.dao;


import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKeyIdentity;


/**
 * DAO Manager for SecurityManager Entity
 */
public interface SecurityDataService
{

    /******************************************
     * Store Public key in the DB
     */
    public void saveKeyIdentityData( String hostId ,String sKeyId,String pKeyId, short type );


    /******************************************
     * Remove Public key from the DB
     */
    public void removeKeyIdentityData( String hostId);


    /******************************************
     * Get SecurityKeyIdentity entity from DB
     */
    public SecurityKeyIdentity getKeyIdentityData( String hostId );


    /******************************************
     *
     */
    SecretKeyStore getSecretKeyData( String fingerprint );


    /******************************************
     *
     */
    void saveSecretKeyData( String fingerprint, byte[] data, String pwd, short type );


    /******************************************
     *
     */
    void removeSecretKeyData( String fingerprint );


    /******************************************
     *
     */
    String getSecretKeyFingerprint( String hostId );
}
