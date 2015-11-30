package io.subutai.core.security.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.security.api.model.SecurityKeyTrust;


/**
 *
 */
@Entity
@Table( name = "security_key_trust",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_fprint", "target_fprint"} ))
@Access( AccessType.FIELD )
public class SecurityKeyTrustEntity implements SecurityKeyTrust
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "source_fprint" )
    private String sourceFingerprint;

    @Column( name = "target_fprint" )
    private String targetFingerprint;

    @Column( name = "level" )
    private int level = 1;


    //**********************************
    @Transient
    private SecurityKey targetKey = null;
    //**********************************


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
    public int getLevel()
    {
        return level;
    }


    @Override
    public void setLevel( final int level )
    {
        this.level = level;
    }


    @Override
    public String getSourceFingerprint()
    {
        return sourceFingerprint;
    }


    @Override
    public void setSourceFingerprint( final String sourceFingerprint )
    {
        this.sourceFingerprint = sourceFingerprint;
    }


    @Override
    public String getTargetFingerprint()
    {
        return targetFingerprint;
    }


    @Override
    public void setTargetFingerprint( final String targetFingerprint )
    {
        this.targetFingerprint = targetFingerprint;
    }

    @Override
    public SecurityKey getTargetKey()
    {
        return targetKey;
    }


    @Override
    public void setTargetKey( final SecurityKey targetKey )
    {
        this.targetKey = targetKey;
    }

}
