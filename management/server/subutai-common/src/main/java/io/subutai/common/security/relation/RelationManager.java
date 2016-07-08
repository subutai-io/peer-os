package io.subutai.common.security.relation;


import java.util.List;

import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;


public interface RelationManager
{

    /**
     * Method for constructing relation parameters
     *
     * @param signedMessage - signed message
     * @param secretKeyId - keyId to verify signed message
     *
     * @deprecated use Challenge {@link io.subutai.common.security.relation.model.RelationChallenge} approach instead.
     */
    @Deprecated
    void processTrustMessage( String signedMessage, final String secretKeyId ) throws RelationVerificationException;


    String getContext();

    /**
     * Used to verify Relation link authenticity by checking signature for generated token with ttl that is passed along
     * for target operation
     */
    String getRelationChallenge( long ttl ) throws RelationVerificationException;

    Relation buildRelation( RelationInfoMeta relationInfoMeta, RelationMeta relationMeta );

    Relation getRelation( RelationMeta relationMeta );

    void saveRelation( Relation relation );

    RelationInfoManager getRelationInfoManager();

    List<Relation> getRelations();

    List<Relation> getRelationsByObject( RelationLink objectRelationLink );

    List<Relation> getRelationsBySource( final RelationLink sourceRelationLink );

    List<Relation> getRelationsByTarget( final RelationLink targetRelationLink );

    void removeRelation( long relationId );

    void removeRelation( RelationMeta relationMeta );

    void removeRelation( RelationLink link );

    RelationLink getRelationLink( String uniqueId );
}
