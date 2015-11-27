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
    public void saveKeyIdentityData( String identityId ,String sKeyId,String pKeyId, int type );


    /******************************************
     * Remove Public key from the DB
     */
    public void removeKeyIdentityData( String identityId);


    /******************************************
     * Get SecurityKeyIdentity entity from DB
     */
    public SecurityKeyIdentity getKeyIdentityData( String identityId );


    /******************************************
     *
     */
    SecretKeyStore getSecretKeyData( String fingerprint );


    /******************************************
     *
     */
    void saveSecretKeyData( String fingerprint, byte[] data, String pwd, int type );


    /******************************************
     *
     */
    void removeSecretKeyData( String fingerprint );


    /******************************************
     *
     */
    void saveKeyTrustData( String sourceId, String targetId, int trustLevel );

    /******************************************
     *
     */
    void removeKeyTrustData( long id );

    /******************************************
     *
     */
    void removeKeyTrustData( String sourceId );


    /******************************************
     *
     */
    void removeKeyTrustData( String sourceId, String targetId );


    /******************************************
     *
     */
    SecurityKeyTrust getKeyTrustData( long id );


    /******************************************
     *
     */
    SecurityKeyTrust getKeyTrustData( String sourceId, String targetId );


    /******************************************
     *
     */
    List<SecurityKeyTrust> getKeyTrustData( String sourceId );
}
