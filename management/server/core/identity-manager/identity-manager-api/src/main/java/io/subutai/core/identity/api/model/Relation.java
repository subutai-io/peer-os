package io.subutai.core.identity.api.model;


import java.io.Serializable;


/**
 * Created by talas on 12/10/15.
 */
public interface Relation extends Serializable
{
    long getId();

    RelationLink getSource();

    RelationLink getTarget();

    RelationLink getTrustedObject();

    RelationInfo getRelationInfo();
}
