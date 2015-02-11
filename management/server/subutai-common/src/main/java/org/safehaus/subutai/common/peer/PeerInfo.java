package org.safehaus.subutai.common.peer;


import java.util.Objects;
import java.util.UUID;


/**
 * Holds info about peer
 */
public class PeerInfo
{
    private String ip = "127.0.0.1";
    private String gatewayIp;

    private PeerStatus status;

    private String name;
    private UUID id;
    private UUID ownerId;
    private int port = 8181;

    private int lastUsedVlanId = 100;
    private String keyId;


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


    public void setPort( final int port )
    {
        this.port = port;
    }


    public UUID getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final UUID ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getGatewayIp()
    {
        return gatewayIp;
    }


    public void setGatewayIp( String gatewayIp )
    {
        this.gatewayIp = gatewayIp;
    }


    public PeerStatus getStatus()
    {
        return status;
    }


    public void setStatus( final PeerStatus status )
    {
        this.status = status;
    }


    public int getPort()
    {
        return port;
    }


    public int getLastUsedVlanId()
    {
        return lastUsedVlanId;
    }


    public void setLastUsedVlanId( int lastUsedVlanId )
    {
        this.lastUsedVlanId = lastUsedVlanId;
    }


    public String getKeyId()
    {
        return keyId;
    }


    public void setKeyId( final String keyId )
    {
        this.keyId = keyId;
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode( this.id );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PeerInfo )
        {
            PeerInfo other = ( PeerInfo ) obj;
            return Objects.equals( this.id, other.id );
        }
        return false;
    }
}

