package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.PeerManager;


public class VNISetupStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;


    public VNISetupStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
    }


    public void execute() throws EnvironmentModificationException, PeerException
    {

        Set<Peer> newPeers = peerManager.resolve( topology.getAllPeers() );
        //remove already participating peers
        newPeers.removeAll( environment.getPeers() );
        newPeers.remove( peerManager.getLocalPeer() );

        //obtain reserved gateways
        Map<Peer, Gateways> reservedGateways = Maps.newHashMap();
        for ( Peer peer : newPeers )
        {
            reservedGateways.put( peer, peer.getGateways() );
        }

        //check availability of subnet
        SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
        String environmentGatewayIp = subnetUtils.getInfo().getLowAddress();

        for ( Map.Entry<Peer, Gateways> peerGateways : reservedGateways.entrySet() )
        {
            Peer peer = peerGateways.getKey();
            Gateways gateways = peerGateways.getValue();

            if ( gateways.findGatewayByIp( environmentGatewayIp ) != null )
            {
                throw new EnvironmentModificationException(
                        String.format( "Subnet %s is already used on peer %s", environment.getSubnetCidr(),
                                peer.getName() ) );
            }
        }

        //TODO: add gateway & p2p IP to reserve vni
        Vni environmentVni = new Vni( environment.getVni(), environment.getId() );

        //check reserved vnis
        for ( final Peer peer : newPeers )
        {
            for ( final Vni vni : peer.getReservedVnis().list() )
            {
                if ( vni.getVni() == environmentVni.getVni() && !Objects
                        .equals( vni.getEnvironmentId(), environmentVni.getEnvironmentId() ) )
                {
                    throw new EnvironmentModificationException(
                            String.format( "Vni %d is already used on peer %s", environment.getVni(),
                                    peer.getName() ) );
                }
            }
        }

        for ( final Peer peer : newPeers )
        {
            peer.reserveVni( environmentVni );
        }
    }
}
