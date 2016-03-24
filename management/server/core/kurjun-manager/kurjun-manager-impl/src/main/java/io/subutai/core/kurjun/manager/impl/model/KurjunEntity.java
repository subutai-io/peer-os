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

    @Column( name = "owner_fprint")
    private String ownerFingerprint;

    @Column( name = "owner_authid" )
    private String authID;

    @Column( name = "owner_message")
    private String signedMessage;

    @Column( name = "token")
    private String token;

    @Column( name = "type")
    private int type = KurjunType.Local.getId();


    @Override
    public String getOwnerFingerprint()
    {
        return ownerFingerprint;
    }


    @Override
    public void setOwnerFingerprint( final String ownerFingerprint )
    {
        this.ownerFingerprint = ownerFingerprint;
    }


    @Override
    public String getAuthID()
    {
        return authID;
    }


    @Override
    public void setAuthID( final String authID )
    {
        this.authID = authID;
    }


    @Override
    public String getSignedMessage()
    {
        return signedMessage;
    }


    @Override
    public void setSignedMessage( final String signedMessage )
    {
        this.signedMessage = signedMessage;
    }


    @Override
    public String getToken()
    {
        return token;
    }


    @Override
    public void setToken( final String token )
    {
        this.token = token;
    }


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
}
