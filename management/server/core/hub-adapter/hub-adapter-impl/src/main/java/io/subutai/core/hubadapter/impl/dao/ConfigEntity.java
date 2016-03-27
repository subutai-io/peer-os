package io.subutai.core.hubadapter.impl.dao;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
//@Table( name = ConfigEntity.TABLE )
@Table( name = "h_config" )
@Access( AccessType.FIELD )
public class ConfigEntity
{
//    @Transient
//    static final String TABLE = "h_config";

    @Id
    @Column( name = "peer_id" )
    private String peerId;

    @Column( name = "server_ip" )
    private String serverIp;

    @Column( name = "user_id" )
    private String ownerId;


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


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }
}
