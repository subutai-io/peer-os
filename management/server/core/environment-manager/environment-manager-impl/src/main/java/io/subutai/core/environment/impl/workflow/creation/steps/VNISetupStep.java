package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.common.peer.LocalPeer;


/**
 * VNI setup generation
 */
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


    public void execute() throws EnvironmentCreationException, PeerException
    {
        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );
        peers.add( localPeer );

        //obtain reserved gateways
        Map<Peer, Set<Gateway>> reservedGateways = Maps.newHashMap();
        for ( Peer peer : peers )
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
                    throw new EnvironmentCreationException(
                            String.format( "Subnet %s is already used on peer %s", environment.getSubnetCidr(),
                                    peer.getName() ) );
                }
            }
        }

        //calculate new vni
        long freeVni = findFreeVni( peers );

        //TODO: add gateway to reserve vni
        Vni newVni = new Vni( freeVni, environment.getId() );

        //reserve new vni and create gateway

        for ( final Peer peer : peers )
        {
            Vni reservedVni = peer.reserveVni( newVni );

            peer.createGateway( new Gateway( reservedVni.getVlan(), environmentGatewayIp ) );
        }

        //store vni in environment metadata
        environment.setVni( freeVni );
    }


    public long findFreeVni( final Set<Peer> peers ) throws EnvironmentCreationException, PeerException
    {

        Set<Long> reservedVnis = Sets.newHashSet();
        for ( Peer peer : peers )
        {
            for ( Vni vni : peer.getReservedVnis() )
            {
                reservedVnis.add( vni.getVni() );
            }
        }

        int maxIterations = 10000;
        int currentIteration = 0;
        long vni;

        do
        {
            vni = ( long ) ( Math.random() * ( Common.MAX_VNI_ID - Common.MIN_VNI_ID ) ) + Common.MIN_VNI_ID;
            currentIteration++;
        }
        while ( reservedVnis.contains( vni ) && currentIteration < maxIterations );

        if ( reservedVnis.contains( vni ) )
        {
            throw new EnvironmentCreationException( "No free vni found" );
        }

        return vni;
    }
}
