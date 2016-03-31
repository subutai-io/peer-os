package io.subutai.core.localpeer.impl.tasks;


import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Tunnel;
import io.subutai.core.network.api.NetworkManager;


public class SetupTunnelsTask implements Callable<Boolean>
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupTunnelsTask.class );
    private final NetworkManager networkManager;
    private final ResourceHost resourceHost;
    private final P2pIps p2pIps;
    private final NetworkResource networkResource;


    public SetupTunnelsTask( final NetworkManager networkManager, final ResourceHost resourceHost, final P2pIps p2pIps,
                             NetworkResource networkResource )
    {
        this.networkManager = networkManager;
        this.resourceHost = resourceHost;
        this.p2pIps = p2pIps;
        this.networkResource = networkResource;
    }


    @Override
    public Boolean call() throws Exception
    {

        Set<Tunnel> tunnels = networkManager.listTunnels( resourceHost );

        Set<VniVlanMapping> mappings = networkManager.getVniVlanMappings( resourceHost );

        //setup tunnel to each local and remote RH
        for ( String tunnelIp : p2pIps.getP2pIps() )
        {
            int tunnelId = findTunnel( tunnelIp, tunnels );
            //tunnel not found, create new one
            if ( tunnelId == -1 )
            {
                //calculate tunnel id
                tunnelId = calculateNextTunnelId( tunnels );

                LOG.debug( String.format( "Setting up tunnel: %s %s", tunnelId, tunnelIp ) );

                //create tunnel
                networkManager.setupTunnel( resourceHost, tunnelId, tunnelIp );

                tunnels.add( new Tunnel( String.format( "%s%d", NetworkManager.TUNNEL_PREFIX, tunnelId ), tunnelIp ) );
            }


            if ( !vniVlanMappingExists( mappings, tunnelId ) )
            {
                //create vni-vlan mapping
                LOG.debug(
                        String.format( "Setting up tunnel %s for %s", tunnelIp, networkResource.getEnvironmentId() ) );

                networkManager.setupVniVLanMapping( resourceHost, tunnelId, networkResource.getVni(),
                        networkResource.getVlan(), networkResource.getEnvironmentId() );

                mappings.add( new VniVlanMapping( tunnelId, networkResource.getVni(), networkResource.getVlan(),
                        networkResource.getEnvironmentId() ) );
            }
        }
        return true;
    }


    public int findTunnel( String tunnelIp, Set<Tunnel> tunnels )
    {
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelIp().equals( tunnelIp ) )
            {
                return tunnel.getTunnelId();
            }
        }

        return -1;
    }


    private int calculateNextTunnelId( Set<Tunnel> tunnels )
    {
        int maxTunnelId = 0;
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelId() > maxTunnelId )
            {
                maxTunnelId = tunnel.getTunnelId();
            }
        }

        return maxTunnelId + 1;
    }


    private boolean vniVlanMappingExists( Set<VniVlanMapping> mappings, int tunnelId )
    {
        for ( VniVlanMapping mapping : mappings )
        {
            if ( mapping.getTunnelId() == tunnelId && mapping.getEnvironmentId()
                                                             .equals( networkResource.getEnvironmentId() ) )
            {
                return true;
            }
        }

        return false;
    }
}
