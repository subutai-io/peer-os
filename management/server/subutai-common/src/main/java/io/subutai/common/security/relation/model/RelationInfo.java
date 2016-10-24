package io.subutai.common.security.relation.model;


import java.io.Serializable;
import java.util.Map;


public interface RelationInfo extends Serializable
{
    long getId();

    int getOwnershipLevel();

    boolean isReadPermission();

    boolean isWritePermission();

    boolean isUpdatePermission();

    boolean isDeletePermission();

    Map<String, String> getRelationTraits();
}
