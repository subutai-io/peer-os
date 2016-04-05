package io.subutai.core.localpeer.impl.tasks;


import java.util.Random;
import java.util.concurrent.Callable;

import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;


public class SetupTunnelsTask implements Callable<Boolean>
{
    private final ResourceHost resourceHost;
    private final P2pIps p2pIps;
    private final NetworkResource networkResource;


    public SetupTunnelsTask( final ResourceHost resourceHost, final P2pIps p2pIps, NetworkResource networkResource )
    {
        this.resourceHost = resourceHost;
        this.p2pIps = p2pIps;
        this.networkResource = networkResource;
    }


    @Override
    public Boolean call() throws Exception
    {

        Tunnels tunnels = resourceHost.getTunnels();


        //setup tunnel to each local and remote RH
        for ( RhP2pIp rhP2pIp : p2pIps.getP2pIps() )
        {
            //skip self
            if ( resourceHost.getId().equalsIgnoreCase( rhP2pIp.getRhId() ) )
            {
                continue;
            }

            //skip if own IP
            boolean ownIp =
                    !( resourceHost.getHostInterfaces().findByIp( rhP2pIp.getP2pIp() ) instanceof NullHostInterface );
            if ( ownIp )
            {
                continue;
            }

            //check p2p connections in case heartbeat hasn't arrived yet with new p2p interface
            P2PConnections p2PConnections = resourceHost.getP2PConnections();
            //skip if exists
            if ( p2PConnections.findByIp( rhP2pIp.getP2pIp() ) != null )
            {
                continue;
            }

            //see if tunnel exists
            Tunnel tunnel = tunnels.findByIp( rhP2pIp.getP2pIp() );

            //create new tunnel
            if ( tunnel == null )
            {
                String tunnelName = generateTunnelName( tunnels );

                if ( tunnelName == null )
                {
                    throw new ResourceHostException( "Free tunnel name not found" );
                }

                Tunnel newTunnel = new Tunnel( tunnelName, rhP2pIp.getP2pIp(), networkResource.getVlan(),
                        networkResource.getVni() );

                resourceHost.createTunnel( newTunnel );

                //add to avoid duplication in the next iteration
                tunnels.addTunnel( newTunnel );
            }
        }

        return true;
    }


    protected String generateTunnelName( Tunnels tunnels )
    {
        int maxIterations = 10000;
        int currentIteration = 0;
        String name;

        Random rnd = new Random();

        do
        {
            int n = 10000 + rnd.nextInt( 90000 );
            name = String.format( "tunnel-%d", n );
            currentIteration++;
        }
        while ( tunnels.findByName( name ) != null && currentIteration < maxIterations );

        if ( tunnels.findByName( name ) != null )
        {
            return null;
        }

        return name;
    }
}
