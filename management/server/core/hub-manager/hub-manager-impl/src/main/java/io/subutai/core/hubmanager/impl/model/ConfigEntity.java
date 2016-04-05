package io.subutai.core.hubmanager.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.hubmanager.api.model.Config;


@Entity
@Table( name = "h_config" )
@Access( AccessType.FIELD )
public class ConfigEntity implements Config
{
    @Id
    @Column( name = "peer_id" )
    private String peerId;

    @Column( name = "server_ip" )
    private String serverIp;

    @Column( name = "user_id" )
    private String ownerId;

    @Column( name = "user_email" )
    private String ownerEmail;


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getHubIp()
    {
        return serverIp;
    }


    public void setHubIp( final String serverIp )
    {
        this.serverIp = serverIp;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getOwnerEmail()
    {
        return ownerEmail;
    }


    public void setOwnerEmail( String ownerEmail )
    {
        this.ownerEmail = ownerEmail;
    }
}
