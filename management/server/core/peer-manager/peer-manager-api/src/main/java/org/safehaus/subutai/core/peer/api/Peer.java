package org.safehaus.subutai.core.peer.api;


import java.util.UUID;


/**
 * Created by bahadyr on 9/6/14.
 */
//@XmlRootElement(name = "Peer")
public class Peer
{

    private String name;
    private String ip;
    private PeerStatus status;
    private UUID id;
    private UUID ownerId;


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public PeerStatus getStatus()
    {
        return status;
    }


    public void setStatus( final PeerStatus status )
    {
        this.status = status;
    }


    public UUID getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final UUID ownerId )
    {
        this.ownerId = ownerId;
    }
}
