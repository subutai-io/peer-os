package io.subutai.core.kurjun.manager.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
    @Column( name = "owner_fprint")
    private String ownerFingerprint;

    @Column( name = "owner_authid" )
    private String authID;

    @Column( name = "owner_message")
    private String signedMessage;

    @Column( name = "token")
    private String token;


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
}
