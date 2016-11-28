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

    @Column( name = "peer_name" )
    private String peerName;


    public ConfigEntity()
    {
    }


    public ConfigEntity( String peerId, String serverIp, String ownerId, String ownerEmail, String peerName )
    {
        this.peerId = peerId;
        this.serverIp = serverIp;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.peerName = peerName;
    }


    @Override
    public String getPeerName()
    {
        return peerName;
    }


    @Override
    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    public String getHubIp()
    {
        return serverIp;
    }


    public void setHubIp( String serverIp )
    {
        this.serverIp = serverIp;
    }


    @Override
    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( String ownerId )
    {
        this.ownerId = ownerId;
    }


    @Override
    public String getOwnerEmail()
    {
        return ownerEmail;
    }


    public void setOwnerEmail( String ownerEmail )
    {
        this.ownerEmail = ownerEmail;
    }
}
