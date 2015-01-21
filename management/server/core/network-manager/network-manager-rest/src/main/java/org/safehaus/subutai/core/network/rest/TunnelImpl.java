package org.safehaus.subutai.core.network.rest;


import org.safehaus.subutai.core.network.api.Tunnel;


class TunnelImpl implements Tunnel
{
    private String tunnelName;
    private String tunnelIp;


    @Override
    public String getTunnelName()
    {
        return tunnelName;
    }


    public void setTunnelName( String tunnelName )
    {
        this.tunnelName = tunnelName;
    }


    @Override
    public String getTunnelIp()
    {
        return tunnelIp;
    }


    public void setTunnelIp( String tunnelIp )
    {
        this.tunnelIp = tunnelIp;
    }


}

