package io.subutai.core.peer.impl.tasks;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.PeerException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.Tunnel;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;


public class SetupTunnelsTask implements Callable<Integer>
{
    private final NetworkManager networkManager;
    private final ManagementHostEntity managementHost;
    private final UUID environmentId;
    private final Set<String> peerIps;


    public SetupTunnelsTask( final NetworkManager networkManager, final ManagementHostEntity managementHost,
                             final UUID environmentId, final Set<String> peerIps )
    {
        this.networkManager = networkManager;
        this.managementHost = managementHost;
        this.environmentId = environmentId;
        this.peerIps = peerIps;
    }


    @Override
    public Integer call() throws Exception
    {
        //fail if vni is not reserved
        Vni environmentVni = managementHost.findVniByEnvironmentId( environmentId );

        if ( environmentVni == null )
        {
            throw new PeerException(
                    String.format( "Error setting up tunnels: No reserved vni found for environment %s",
                            environmentId ) );
        }

        //setup tunnels to each remote peer
        Set<Tunnel> tunnels = networkManager.listTunnels();

        //remove local IP, just in case
        peerIps.remove( managementHost.getExternalIp() );

        for ( String peerIp : peerIps )
        {
            int tunnelId = managementHost.findTunnel( peerIp, tunnels );
            //tunnel not found, create new one
            if ( tunnelId == -1 )
            {
                //calculate tunnel id
                tunnelId = managementHost.calculateNextTunnelId( tunnels );

                //create tunnel
                networkManager.setupTunnel( tunnelId, peerIp );
            }

            //create vni-vlan mapping
            managementHost.setupVniVlanMapping( tunnelId, environmentVni.getVni(), environmentVni.getVlan(),
                    environmentVni.getEnvironmentId() );
        }

        return environmentVni.getVlan();
    }
}
