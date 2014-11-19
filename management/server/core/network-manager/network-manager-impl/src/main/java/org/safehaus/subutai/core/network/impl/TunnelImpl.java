package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.core.network.api.Tunnel;


/**
 * Tunnel implementation
 */
public class TunnelImpl implements Tunnel
{
    private final String tunnelName;
    private final String tunnelIp;


    public TunnelImpl( final String tunnelName, final String tunnelIp )
    {
        this.tunnelName = tunnelName;
        this.tunnelIp = tunnelIp;
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
}
