package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.Tunnel;

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
}
