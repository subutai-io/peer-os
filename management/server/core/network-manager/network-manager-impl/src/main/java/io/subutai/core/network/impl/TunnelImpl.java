package io.subutai.core.network.impl;


import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.Tunnel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Tunnel implementation
 */
public class TunnelImpl implements Tunnel
{
    private final String tunnelName;
    private final String tunnelIp;
    private final int tunnelId;


    public TunnelImpl( final String tunnelName, final String tunnelIp )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelName ) );
        Preconditions.checkArgument( tunnelName.matches( String.format( "%s\\d+", NetworkManager.TUNNEL_PREFIX ) ) );

        this.tunnelName = tunnelName;
        this.tunnelIp = tunnelIp;
        this.tunnelId = Integer.parseInt( tunnelName.replace( NetworkManager.TUNNEL_PREFIX, "" ) );
    }


    @Override
    public String getTunnelName()
    {
        return tunnelName;
    }


    @Override
    public String getTunnelIp()
    {
        return tunnelIp;
    }


    @Override
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
        if ( !( o instanceof TunnelImpl ) )
        {
            return false;
        }

        final TunnelImpl tunnel = ( TunnelImpl ) o;

        if ( tunnelId != tunnel.tunnelId )
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
        int result = tunnelName.hashCode();
        result = 31 * result + tunnelIp.hashCode();
        result = 31 * result + tunnelId;
        return result;
    }
}
