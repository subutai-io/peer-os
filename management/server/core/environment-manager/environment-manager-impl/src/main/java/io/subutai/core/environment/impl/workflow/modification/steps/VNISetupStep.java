package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.common.peer.LocalPeer;


public class VNISetupStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final LocalPeer localPeer;


    public VNISetupStep( final Topology topology, final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.topology = topology;
        this.environment = environment;
        this.localPeer = localPeer;
    }


    public void execute() throws EnvironmentModificationException, PeerException
    {

        Set<Peer> newPeers = Sets.newHashSet( topology.getAllPeers() );
        //remove already participating peers
        newPeers.removeAll( environment.getPeers() );
        newPeers.remove( localPeer );

        //obtain reserved gateways
        Map<Peer, Set<Gateway>> reservedGateways = Maps.newHashMap();
        for ( Peer peer : newPeers )
        {
            reservedGateways.put( peer, peer.getGateways() );
        }

        //check availability of subnet
        SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
        String environmentGatewayIp = subnetUtils.getInfo().getLowAddress();

        for ( Map.Entry<Peer, Set<Gateway>> peerGateways : reservedGateways.entrySet() )
        {
            Peer peer = peerGateways.getKey();
            Set<Gateway> gateways = peerGateways.getValue();
            for ( Gateway gateway : gateways )
            {
                if ( gateway.getIp().equals( environmentGatewayIp ) )
                {
                    throw new EnvironmentModificationException(
                            String.format( "Subnet %s is already used on peer %s", environment.getSubnetCidr(),
                                    peer.getName() ) );
                }
            }
        }

        Vni environmentVni = new Vni( environment.getVni(), environment.getId() );

        //check reserved vnis

        for ( final Peer peer : newPeers )
        {
            for ( final Vni vni : peer.getReservedVnis() )
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

        //try to reserve vni and create gateway

        for ( final Peer peer : newPeers )
        {
            Vni reservedVni = peer.reserveVni( environmentVni );

            peer.createGateway( new Gateway( reservedVni.getVlan(), environmentGatewayIp ) );
        }
    }
}
