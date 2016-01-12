package io.subutai.core.identity.api.relation;


import java.util.List;
import java.util.Set;

import io.subutai.core.identity.api.exception.RelationVerificationException;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationInfoMeta;
import io.subutai.core.identity.api.model.RelationLink;
import io.subutai.core.identity.api.model.RelationMeta;


/**
 * Created by talas on 12/7/15.
 */
public interface RelationManager
{
    String getContext();

    void processTrustMessage( String signedMessage, final String secretKeyId ) throws RelationVerificationException;

    RelationInfo createTrustRelationship( String context, Set<String> operation, int ownershipLevel );

    RelationInfo createTrustRelationship( RelationInfoMeta relationInfoMeta );

    Relation buildTrustRelation( RelationInfo relationInfo, RelationMeta relationMeta );

    Relation getRelation(RelationMeta relationMeta);

    void saveRelation( Relation relation );

    RelationInfoManager getRelationInfoManager();

    RelationLink getRelationLink( String uniqueId, String objectClass );

    List<Relation> getRelationsByObject( RelationLink objectRelationLink );

    void removeRelation( long relationId );
}
