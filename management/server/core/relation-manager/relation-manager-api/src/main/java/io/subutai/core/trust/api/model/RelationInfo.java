package io.subutai.core.trust.api.model;


/**
 * Created by talas on 12/10/15.
 */
public interface RelationInfo
{
    long getId();

    int getOwnershipLevel();

    boolean isReadPermission();

    boolean isWritePermission();

    boolean isUpdatePermission();

    boolean isDeletePermission();
}
