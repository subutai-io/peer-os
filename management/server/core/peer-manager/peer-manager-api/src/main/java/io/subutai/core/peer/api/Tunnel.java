package io.subutai.core.peer.api;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Represents tunnel between peers
 */
public class Tunnel
{
    public static final String TUNNEL_PREFIX = "tunnel";
    private String peerId;
    private final String tunnelName;
    private final String tunnelIp;
    private final int tunnelId;


    public Tunnel( final String tunnelName, final String tunnelIp )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelName ) );
        Preconditions.checkArgument( tunnelName.matches( String.format( "%s\\d+", TUNNEL_PREFIX ) ) );

        this.tunnelName = tunnelName;
        this.tunnelIp = tunnelIp;
        this.tunnelId = Integer.parseInt( tunnelName.replace( TUNNEL_PREFIX, "" ) );
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getTunnelName()
    {
        return tunnelName;
    }


    public String getTunnelIp()
    {
        return tunnelIp;
    }


    public int getTunnelId()
    {
        return tunnelId;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Tunnel ) )
        {
            return false;
        }

        final Tunnel tunnel = ( Tunnel ) o;

        if ( tunnelId != tunnel.tunnelId )
        {
            return false;
        }
        if ( !peerId.equals( tunnel.peerId ) )
        {
            return false;
        }
        if ( !tunnelName.equals( tunnel.tunnelName ) )
        {
            return false;
        }
        return tunnelIp.equals( tunnel.tunnelIp );
    }


    @Override
    public int hashCode()
    {
        int result = peerId.hashCode();
        result = 31 * result + tunnelName.hashCode();
        result = 31 * result + tunnelIp.hashCode();
        result = 31 * result + tunnelId;
        return result;
    }
}