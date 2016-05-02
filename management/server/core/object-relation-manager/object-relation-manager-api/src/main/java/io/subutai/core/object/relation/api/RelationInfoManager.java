package io.subutai.core.object.relation.api;


import io.subutai.common.security.relation.RelationLink;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;


/**
 * The reason why in relation chain must participate three elements because each link will have relation info and for
 * transitional checks we will verify that each relation link does have authority to build new relation
 */
public interface RelationInfoManager
{
    void checkRelation( final RelationLink targetObject,
                                final RelationInfoMeta relationInfoMeta, final String encodedToken )
            throws RelationVerificationException;

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

    /**
     * Used to check group update permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean groupHasUpdatePermissions( RelationLink relationLink );

    /**
     * Used to check all read permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean allHasReadPermissions( RelationLink relationLink );

    /**
     * Used to check all write permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean allHasWritePermissions( RelationLink relationLink );

    /**
     * Used to check all delete permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean allHasDeletePermissions( RelationLink relationLink );

    /**
     * Used to check all update permissions
     *
     * @deprecated use {@link #checkRelation(RelationLink, RelationLink, RelationInfoMeta, String)} ()} instead.
     */
    @Deprecated
    boolean allHasUpdatePermissions( RelationLink relationLink );
}
