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

    // TODO should be omitted so that only system can change relation status
    void setRelationStatus( final RelationStatus relationStatus );

    String getKeyId();
}
