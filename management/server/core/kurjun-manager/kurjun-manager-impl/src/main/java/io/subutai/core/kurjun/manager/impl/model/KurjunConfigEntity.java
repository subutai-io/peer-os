package io.subutai.core.kurjun.manager.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.kurjun.manager.api.model.KurjunConfig;


@Entity
@Table( name = "kurjun_config" )
@Access( AccessType.FIELD )
public class KurjunConfigEntity implements KurjunConfig
{
    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column( name = "owner_fprint" )
    private String ownerFingerprint;

    @Column( name = "owner_authid" )
    private String authID;

    @Column( name = "type" )
    private int type = KurjunType.Local.getId();


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public String getOwnerFingerprint()
    {
        return ownerFingerprint;
    }


    public void setOwnerFingerprint( final String ownerFingerprint )
    {
        this.ownerFingerprint = ownerFingerprint;
    }


    public String getAuthID()
    {
        return authID;
    }


    public void setAuthID( final String authID )
    {
        this.authID = authID;
    }


    public int getType()
    {
        return type;
    }


    public void setType( final int type )
    {
        this.type = type;
    }
}
