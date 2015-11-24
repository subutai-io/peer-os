package io.subutai.core.security.api.dao;


import java.util.List;

import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.api.model.SecurityKeyTrust;


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


    void saveKeyTrustData( String sourceId, String targetId, int trustLevel );

    void removeKeyTrustData( long id );

    void removeKeyTrustData( String sourceId );

    void removeKeyTrustData( String sourceId, String targetId );

    SecurityKeyTrust getKeyTrustData( long id );

    List<SecurityKeyTrust> getKeyTrustData( String sourceId );
}
