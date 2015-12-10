package io.subutai.core.identity.api.relation;


import java.util.Map;
import java.util.Set;

import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationMeta;


/**
 * Created by talas on 12/7/15.
 */
public interface TrustRelationManager
{
    void processTrustMessage( String encrypted );

    void createTrustRelationship( Map<String, String> relationshipProp );

    boolean isRelationValid( String sourceId, String sourcePath, String objectId, String objectPath, String statement );

    RelationInfo generateTrustRelationship( String pObject, Set<String> operation, String type );

    Relation buildTrustRelation( RelationInfo relationInfo, RelationMeta relationMeta );

    void executeRelationBuild( Relation relation );
}
