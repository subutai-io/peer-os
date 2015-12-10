package io.subutai.core.identity.api.relation;


import java.util.Map;


/**
 * Created by talas on 12/7/15.
 */
public interface TrustRelationManager
{
    void processTrustMessage( String encrypted );

    void createTrustRelationship( Map<String, String> relationshipProp );

    boolean isRelationValid( String sourceId, String sourcePath, String objectId, String objectPath, String statement );
}
