package org.safehaus.subutai.core.environment.ui.manage;


import org.safehaus.subutai.core.network.api.Tunnel;


class TunnelImpl implements Tunnel
{
    String tunnelName;
    String tunnelIp;


    public static TunnelImpl newCopy( Tunnel tunnel )
    {
        TunnelImpl t = new TunnelImpl();
        if ( tunnel != null )
        {
            t.tunnelName = tunnel.getTunnelName();
            t.tunnelIp = tunnel.getTunnelIp();
        }
        return t;
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

