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
    public void saveKey( String hostId, String keyId , short type );


    /******************************************
     * Remove Public key from the DB
     */
    public void removeKey( String hostId);


    /******************************************
     * Get SecurityKeyIdentity entity from DB
     */
    public SecurityKeyIdentity getSecurityKeyIdentity( String hostId );


    /******************************************
     * Get Security KeyId from DB
     */
    public String getKeyId( String hostId );

}
