package io.subutai.core.identity.api.relation;


import java.util.Set;

import io.subutai.core.identity.api.exception.RelationVerificationException;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationMeta;


/**
 * Created by talas on 12/7/15.
 */
public interface RelationManager
{
    void processTrustMessage( String signedMessage, final String secretKeyId ) throws RelationVerificationException;

    RelationInfo generateTrustRelationship( String pObject, Set<String> operation, int ownershipLevel );

    Relation buildTrustRelation( RelationInfo relationInfo, RelationMeta relationMeta );

    Relation getRelation(RelationMeta relationMeta);

    void executeRelationBuild( Relation relation );

    RelationInfoManager getRelationInfoManager();
}
