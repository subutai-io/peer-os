package io.subutai.core.localpeer.impl.tasks;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.peer.ManagementHost;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


public class SetupTunnelsTask implements Callable<Integer>
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupTunnelsTask.class );
    private final NetworkManager networkManager;
    private final LocalPeer localPeer;
    private final String environmentId;
    private final Map<String, String> peerIps;


    public SetupTunnelsTask( final NetworkManager networkManager, final LocalPeer localPeer, final String environmentId,
                             final Map<String, String> peerIps )
    {
        this.networkManager = networkManager;
        this.localPeer = localPeer;
        this.environmentId = environmentId;
        this.peerIps = peerIps;
    }


    @Override
    public Integer call() throws Exception
    {
        //fail if vni is not reserved
        Vni environmentVni = localPeer.findVniByEnvironmentId( environmentId );

        if ( environmentVni == null )
        {
            throw new PeerException(
                    String.format( "Error setting up tunnels: No reserved vni found for environment %s",
                            environmentId ) );
        }

        for ( String peerIp : peerIps.keySet() )
        {
            if ( peerIp.equals( localPeer.getId() ) )
            {
                LOG.debug( "Skiping local peer." );
                continue;
            }

            LOG.debug( String.format( "Setting up tunnel on : %s", peerIp ) );

            //setup tunnels to each remote peer
            Set<Tunnel> tunnels = networkManager.listTunnels();

            String tunnelIp = peerIps.get( peerIp );
            int tunnelId = findTunnel( tunnelIp, tunnels );
            //tunnel not found, create new one
            if ( tunnelId == -1 )
            {
                //calculate tunnel id
                tunnelId = calculateNextTunnelId( tunnels );


                LOG.debug( String.format( "Setting up tunnel: %s %s", tunnelId, tunnelIp ) );
                //create tunnel
                networkManager.setupTunnel( tunnelId, tunnelIp );
            }

            //create vni-vlan mapping
            LOG.debug( String.format( "Setting up tunnel for %s: %s", peerIp, environmentVni ) );
            setupVniVlanMapping( tunnelId, environmentVni.getVni(), environmentVni.getVlan(),
                    environmentVni.getEnvironmentId() );
        }

        return environmentVni.getVlan();
    }

    public int findTunnel( String peerIp, Set<Tunnel> tunnels )
    {
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelIp().equals( peerIp ) )
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


    private void setupVniVlanMapping( final int tunnelId, final long vni, final int vlanId, final String environmentId )
            throws PeerException
    {
        try
        {
            Set<VniVlanMapping> mappings = networkManager.getVniVlanMappings();

            for ( VniVlanMapping mapping : mappings )
            {
                if ( mapping.getTunnelId() == tunnelId && mapping.getEnvironmentId().equals( environmentId ) )
                {
                    return;
                }
            }

            networkManager.setupVniVLanMapping( tunnelId, vni, vlanId, environmentId );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }
}
