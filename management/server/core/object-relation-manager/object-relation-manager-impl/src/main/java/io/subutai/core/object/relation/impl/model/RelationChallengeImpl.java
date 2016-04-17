package io.subutai.core.object.relation.impl.model;


import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.object.relation.api.model.RelationChallenge;
import io.subutai.core.object.relation.api.model.RelationStatus;


/**
 * Created by ape-craft on 3/18/16.
 */
@Entity
@Table( name = "relation_challenge" )
@Access( AccessType.FIELD )
public class RelationChallengeImpl implements RelationChallenge
{
    @Id
    @Column( name = "rl_challenge" )
    private String token;

    @Column( name = "timestamp" )
    private long timestamp;

    @Column( name = "ttl" )
    private long ttl;

    @Enumerated( EnumType.STRING )
    @Column( name = "status" )
    private RelationStatus status;


    public RelationChallengeImpl()
    {
        this.ttl = -1;
        this.status = RelationStatus.STATED;
    }


    public RelationChallengeImpl( final long ttl )
    {
        this.token = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.ttl = ttl;
        this.status = RelationStatus.STATED;
    }


    public String getToken()
    {
        return token;
    }


    public long getTimestamp()
    {
        return timestamp;
    }


    public long getTtl()
    {
        return ttl;
    }


    public void setTtl( final long ttl )
    {
        this.ttl = ttl;
    }


    public RelationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RelationStatus status )
    {
        this.status = status;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final RelationChallengeImpl that = ( RelationChallengeImpl ) o;

        if ( timestamp != that.timestamp )
        {
            return false;
        }
        if ( ttl != that.ttl )
        {
            return false;
        }
        return token != null ? token.equals( that.token ) : that.token == null;
    }


    @Override
    public int hashCode()
    {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + ( int ) ( timestamp ^ ( timestamp >>> 32 ) );
        result = 31 * result + ( int ) ( ttl ^ ( ttl >>> 32 ) );
        return result;
    }


    @Override
    public String toString()
    {
        return "RelationChallengeImpl{" +
                "token='" + token + '\'' +
                ", timestamp=" + timestamp +
                ", ttl=" + ttl +
                ", status=" + status +
                '}';
    }
}
