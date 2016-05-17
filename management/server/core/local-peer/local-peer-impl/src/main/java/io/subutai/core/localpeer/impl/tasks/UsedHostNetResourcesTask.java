package io.subutai.core.localpeer.impl.tasks;


import java.util.regex.Matcher;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostInterface;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
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

        for ( HostInterface iface : resourceHost.getHostInterfaces().getAll() )
        {
            //container subnet
            Matcher matcher = Common.GATEWAY_INTERFACE_NAME_PATTERN.matcher( iface.getName().trim() );
            if ( matcher.find() )
            {
                usedNetworkResources.addContainerSubnet( iface.getIp() );
                usedNetworkResources.addVlan( Integer.parseInt( matcher.group( 1 ) ) );
            }

            //p2p subnet
            matcher = Common.P2P_INTERFACE_NAME_PATTERN.matcher( iface.getName().trim() );
            if ( matcher.find() )
            {
                usedNetworkResources.addP2pSubnet( iface.getIp() );
                usedNetworkResources.addVlan( Integer.parseInt( matcher.group( 1 ) ) );
            }

            //add LAN subnet to prevent collisions
            if ( iface.getName().equalsIgnoreCase( SystemSettings.getExternalIpInterface() ) )
            {
                usedNetworkResources.addContainerSubnet( iface.getIp() );
                usedNetworkResources.addP2pSubnet( iface.getIp() );
            }

            //add all supplementary network interfaces to exclusion list
            if ( iface.getName().startsWith( "eth" ) )
            {
                usedNetworkResources.addContainerSubnet( iface.getIp() );
                usedNetworkResources.addP2pSubnet( iface.getIp() );
            }
        }

        return null;
    }
}
