package io.subutai.common.security.relation;


import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;


/**
 * The reason why in relation chain must participate three elements because each link will have relation info and for
 * transitional checks we will verify that each relation link does have authority to build new relation
 */
public interface RelationInfoManager
{
    void checkRelation( final RelationLink targetObject, final RelationInfoMeta relationInfoMeta,
                        final String encodedToken ) throws RelationVerificationException;

    void checkRelation( final RelationLink source, final RelationLink targetObject,
                        final RelationInfoMeta relationInfoMeta, final String encodedToken )
            throws RelationVerificationException;

    /**
     * Used to check group write permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean groupHasWritePermissions( RelationMeta relationMeta );

    /**
     * Used to check all read permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean allHasReadPermissions( RelationMeta relationMeta );
}
