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


    public PeerStatus getStatus()
    {
        return status;
    }


    public void setStatus( final PeerStatus status )
    {
        this.status = status;
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


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    @Override
    public String toString()
    {
        return "Peer{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
