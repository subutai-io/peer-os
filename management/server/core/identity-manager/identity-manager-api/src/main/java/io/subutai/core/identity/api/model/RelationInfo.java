package io.subutai.core.identity.api.model;


import java.util.Set;


/**
 * Created by talas on 12/10/15.
 */
public interface RelationInfo
{
    long getId();

    String getContext();

    Set<String> getOperation();

    int getOwnershipLevel();
}
