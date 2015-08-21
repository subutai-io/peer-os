package io.subutai.core.security.api.dao;


import io.subutai.core.security.api.model.SecurityKeyIdentity;


/**
 * DAO Manager for SecurityManager Entity
 */
public interface SecurityManagerDAO
{

    /******************************************
     * Store Public key in the DB
     */
    public void saveKeyIdentityData( String hostId, String keyId , short type );


    /******************************************
     * Remove Public key from the DB
     */
    public void removeKeyIdentityData( String hostId);


    /******************************************
     * Get SecurityKeyIdentity entity from DB
     */
    public SecurityKeyIdentity getKeyIdentityData( String hostId );


    /******************************************
     * Get Security KeyId from DB
     */
    public String getKeyFingerprint( String hostId );

}
