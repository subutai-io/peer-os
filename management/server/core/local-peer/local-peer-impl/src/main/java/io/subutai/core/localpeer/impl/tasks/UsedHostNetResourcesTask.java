package io.subutai.core.localpeer.impl.tasks;


import com.google.common.base.Preconditions;

import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.util.HostUtil;


public class UsedHostNetResourcesTask extends HostUtil.Task<Object>
{
    private final ResourceHost resourceHost;
    private final UsedNetworkResources usedNetworkResources;


    public UsedHostNetResourcesTask( final ResourceHost resourceHost, final UsedNetworkResources usedNetworkResources )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( usedNetworkResources );

        this.resourceHost = resourceHost;
        this.usedNetworkResources = usedNetworkResources;
    }


    @Override
    public int maxParallelTasks()
    {
        return 0;
    }


    @Override
    public String name()
    {
        return "Obtain used network resources";
    }


    @Override
    public Object call() throws Exception
    {
        //tunnels
        Tunnels tunnels = resourceHost.getTunnels();
        for ( Tunnel tunnel : tunnels.getTunnels() )
        {
            usedNetworkResources.addVni( tunnel.getVni() );
            usedNetworkResources.addVlan( tunnel.getVlan() );
            usedNetworkResources.addP2pSubnet( tunnel.getTunnelIp() );
        }

        //p2p connections
        P2PConnections p2PConnections = resourceHost.getP2PConnections();
        for ( P2PConnection p2PConnection : p2PConnections.getConnections() )
        {
            usedNetworkResources.addP2pSubnet( p2PConnection.getIp() );
        }

        return null;
    }
}
