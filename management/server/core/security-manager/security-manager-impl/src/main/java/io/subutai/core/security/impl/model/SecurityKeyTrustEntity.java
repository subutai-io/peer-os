package io.subutai.core.security.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.subutai.core.security.api.model.SecurityKeyTrust;


/**
 *
 */
@Entity
@Table( name = "security_key_trust",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_id", "target_id"} ))
@Access( AccessType.FIELD )
public class SecurityKeyTrustEntity implements SecurityKeyTrust
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "source_id" )
    private String sourceId;

    @Column( name = "target_id" )
    private String targetId;

    @Column( name = "level" )
    private int level = 1;


    @Override
    public long getId()
    {
        return id;
    }


    @Override
    public void setId( final long id )
    {
        this.id = id;
    }


    @Override
    public String getSourceId()
    {
        return sourceId;
    }


    @Override
    public void setSourceId( final String sourceId )
    {
        this.sourceId = sourceId;
    }


    @Override
    public String getTargetId()
    {
        return targetId;
    }


    @Override
    public void setTargetId( final String targetId )
    {
        this.targetId = targetId;
    }


    @Override
    public int getLevel()
    {
        return level;
    }


    @Override
    public void setLevel( final int level )
    {
        this.level = level;
    }
}
