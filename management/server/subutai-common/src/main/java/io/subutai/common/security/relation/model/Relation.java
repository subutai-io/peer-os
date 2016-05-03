package io.subutai.common.security.relation.model;


import java.io.Serializable;

import io.subutai.common.security.relation.RelationLink;


public interface Relation extends Serializable
{
    long getId();

    RelationLink getSource();

    RelationLink getTarget();

    RelationLink getTrustedObject();

    RelationInfo getRelationInfo();

    RelationStatus getRelationStatus();

    void setRelationStatus( final RelationStatus relationStatus );

    String getKeyId();
}
