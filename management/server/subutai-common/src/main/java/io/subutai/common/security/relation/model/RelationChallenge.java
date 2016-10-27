package io.subutai.common.security.relation.model;


import java.io.Serializable;


public interface RelationChallenge extends Serializable
{
    String getToken();

    long getTimestamp();

    long getTtl();

    void setTtl( final long ttl );

    RelationStatus getStatus();

    void setStatus( final RelationStatus status );
}
