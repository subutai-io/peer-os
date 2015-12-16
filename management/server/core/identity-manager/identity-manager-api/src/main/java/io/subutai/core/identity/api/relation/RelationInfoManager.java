package io.subutai.core.identity.api.relation;


import io.subutai.core.identity.api.model.RelationMeta;


/**
 * Created by talas on 12/7/15.
 */


/**
 * The reason why in relation chain must participate three elements because each link will have relation info and for
 * transitional checks we will verify that each relation link does have authority to build new relation
 */
public interface RelationInfoManager
{
    boolean ownerHasReadPermissions( RelationMeta relationMeta );

    boolean ownerHasWritePermissions( RelationMeta relationMeta );

    boolean ownerHasDeletePermissions( RelationMeta relationMeta );

    boolean ownerHasUpdatePermissions( RelationMeta relationMeta );

    boolean groupHasReadPermissions( RelationMeta relationMeta );

    boolean groupHasWritePermissions( RelationMeta relationMeta );

    boolean groupHasDeletePermissions( RelationMeta relationMeta );

    boolean groupHasUpdatePermissions( RelationMeta relationMeta );

    boolean allHasReadPermissions( RelationMeta relationMeta );

    boolean allHasWritePermissions( RelationMeta relationMeta );

    boolean allHasDeletePermissions( RelationMeta relationMeta );

    boolean allHasUpdatePermissions( RelationMeta relationMeta );
}
