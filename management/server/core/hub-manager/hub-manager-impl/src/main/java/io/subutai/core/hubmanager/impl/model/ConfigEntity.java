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


    public ConfigEntity()
    {
    }


    public ConfigEntity( String peerId, String serverIp, String ownerId, String ownerEmail )
    {
        this.peerId = peerId;
        this.serverIp = serverIp;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
    }


    @Override
    public String getPeerId()
    {
        return peerId;
    }


    @Override
    public void setPeerId( String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    public String getHubIp()
    {
        return serverIp;
    }


    @Override
    public void setHubIp( String serverIp )
    {
        this.serverIp = serverIp;
    }


    @Override
    public String getOwnerId()
    {
        return ownerId;
    }


    @Override
    public void setOwnerId( String ownerId )
    {
        this.ownerId = ownerId;
    }


    @Override
    public String getOwnerEmail()
    {
        return ownerEmail;
    }


    @Override
    public void setOwnerEmail( String ownerEmail )
    {
        this.ownerEmail = ownerEmail;
    }
}
