package io.subutai.common.security.relation.model;


import java.io.Serializable;


/**
 * Created by ape-craft on 3/19/16.
 */
public interface RelationChallenge extends Serializable
{
    public String getToken();

    public long getTimestamp();

    public long getTtl();

    public void setTtl( final long ttl );

    public RelationStatus getStatus();

    public void setStatus( final RelationStatus status );
}
