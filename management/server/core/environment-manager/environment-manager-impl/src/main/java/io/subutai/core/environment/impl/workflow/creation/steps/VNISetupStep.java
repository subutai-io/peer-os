package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;


/**
 * VNI setup generation
 */
public class VNISetupStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( VNISetupStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;


    public VNISetupStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
    }


    public void execute() throws EnvironmentCreationException, PeerException
    {
        LOGGER.debug( "VNI setup started..." );

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        peers.add( peerManager.getLocalPeer() );

        //obtain reserved gateways
        Map<Peer, Gateways> reservedGateways = Maps.newHashMap();
        for ( Peer peer : peers )
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
                throw new EnvironmentCreationException(
                        String.format( "Subnet %s is already used on peer %s", environment.getSubnetCidr(),
                                peer.getName() ) );
            }
        }

        LOGGER.debug( "Find free VNI..." );
        //calculate new vni
        long freeVni = findFreeVni( peers );

        //TODO: add gateway & p2p IP to reserve vni
        Vni newVni = new Vni( freeVni, environment.getId() );

        //reserve new vni
        for ( final Peer peer : peers )
        {
            peer.reserveVni( newVni );
        }

        //store vni in environment metadata
        environment.setVni( freeVni );
    }


    public long findFreeVni( final Set<Peer> peers ) throws EnvironmentCreationException, PeerException
    {

        Set<Long> reservedVnis = Sets.newHashSet();
        for ( Peer peer : peers )
        {
            for ( Vni vni : peer.getReservedVnis().list() )
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
