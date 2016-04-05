package io.subutai.core.kurjun.manager.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.kurjun.manager.api.model.Kurjun;


/**
 * Implementation of Kurjun interface. Used for storing kurjun data.
 */
@Entity
@Table( name = "kurjun_data" )
@Access( AccessType.FIELD )
public class KurjunEntity implements Kurjun
{

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "url" )
    private String url;

    @Column( name = "state" )
    private boolean state;

    @Column( name = "type" )
    private int type = KurjunType.Local.getId();

    @Column( name = "owner_fprint")
    private String ownerFingerprint;

    @Column( name = "owner_authid" )
    private String authID;

    @Column( name = "owner_message")
    private byte[] signedMessage;

    @Column( name = "token")
    private String token;


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
    public int getType()
    {
        return type;
    }


    @Override
    public void setType( final int type )
    {
        this.type = type;
    }


    public String getUrl()
    {
        return url;
    }


    public void setUrl( final String url )
    {
        this.url = url;
    }


    public boolean getState()
    {
        return state;
    }


    public void setState( final boolean state )
    {
        this.state = state;
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


    public String getToken()
    {
        return token;
    }


    public void setToken( final String token )
    {
        this.token = token;
    }


    @Override
    public byte[] getSignedMessage()
    {
        return signedMessage;
    }


    public void setSignedMessage( final byte[] signedMessage )
    {
        this.signedMessage = signedMessage;
    }
}
