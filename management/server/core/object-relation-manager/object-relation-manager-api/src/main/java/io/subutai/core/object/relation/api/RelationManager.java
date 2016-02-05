package io.subutai.core.object.relation.api;


import java.util.List;

import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfo;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationLink;
import io.subutai.core.object.relation.api.model.RelationMeta;


/**
 * Created by talas on 12/7/15.
 */
public interface RelationManager
{
    String getContext();

    void processTrustMessage( String signedMessage, final String secretKeyId ) throws RelationVerificationException;

    RelationInfo createTrustRelationship( RelationInfoMeta relationInfoMeta );

    Relation buildTrustRelation( RelationInfo relationInfo, RelationMeta relationMeta );

    Relation getRelation(RelationMeta relationMeta);

    void saveRelation( Relation relation );

    RelationInfoManager getRelationInfoManager();

    RelationLink getRelationLink( String uniqueId, String objectClass );

    List<Relation> getRelationsByObject( RelationLink objectRelationLink );

    void removeRelation( long relationId );
}
