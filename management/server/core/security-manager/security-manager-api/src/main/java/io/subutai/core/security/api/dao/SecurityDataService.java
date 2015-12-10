package io.subutai.core.security.api.dao;


import java.util.List;
import java.util.Map;

import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.security.api.model.SecurityKeyTrust;
import io.subutai.core.security.api.model.TrustItem;
import io.subutai.core.security.api.model.TrustRelation;


/**
 * DAO Manager for SecurityManager Entity
 */
public interface SecurityDataService
{

    /******************************************
     * Store Public key in the DB
     */
    public void saveKeyData( String identityId, String sKeyId, String pKeyId, int type );


    /******************************************
     * Remove Public key from the DB
     */
    public void removeKeyData( String identityId );


    /******************************************
     * Get SecurityKey entity from DB
     */
    public SecurityKey getKeyData( String identityId );


    /******************************************
     * Get SecurityKey entity from DB
     */
    public SecurityKey getKeyDataByFingerprint( String fingerprint );


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
    SecurityKeyTrust saveKeyTrustData( String sourceId, String targetId, int trustLevel );


    /******************************************
     *
     */
    public void updateKeyTrustData( SecurityKeyTrust securityKeyTrust );


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
    void removeKeyAllTrustData( String fingerprint );


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


    void createTrustRelationship( Map<String, String> relationshipProp );


    TrustItem getTrustItem( String uniqueIdentifier, String classPath );

    TrustRelation getTrustRelationBySourceObject( TrustItem source, TrustItem object );
}
