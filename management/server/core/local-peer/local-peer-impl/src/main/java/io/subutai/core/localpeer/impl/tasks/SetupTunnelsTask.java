package io.subutai.core.localpeer.impl.tasks;


import java.util.concurrent.Callable;

import io.subutai.common.host.NullHostInterface;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.core.network.api.NetworkManager;


public class SetupTunnelsTask implements Callable<Boolean>
{
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

        Tunnels tunnels = networkManager.getTunnels( resourceHost );


        //setup tunnel to each local and remote RH
        for ( String tunnelIp : p2pIps.getP2pIps() )
        {
            //skip if own IP
            boolean ownIp = !( resourceHost.getHostInterfaces().findByIp( tunnelIp ) instanceof NullHostInterface );
            if ( ownIp )
            {
                continue;
            }

            //see if tunnel exists
            Tunnel tunnel = tunnels.findByIp( tunnelIp );

            //create new tunnel
            if ( tunnel == null )
            {
                Tunnel newTunnel = new Tunnel( String.format( "tunnel-%d", networkResource.getVlan() ), tunnelIp,
                        networkResource.getVlan(), networkResource.getVni() );

                networkManager.createTunnel( resourceHost, newTunnel );

                //add to avoid duplication in the next iteration
                tunnels.addTunnel( newTunnel );
            }
        }

        return true;
    }
}
