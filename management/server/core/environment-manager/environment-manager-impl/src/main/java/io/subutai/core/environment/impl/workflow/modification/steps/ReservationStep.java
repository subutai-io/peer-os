package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.environment.impl.workflow.PeerUtil;
import io.subutai.core.peer.api.PeerManager;


public class ReservationStep
{

    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final TrackerOperation trackerOperation;


    public ReservationStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                            final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentModificationException, PeerException
    {

        Set<Peer> newPeers = peerManager.resolve( topology.getAllPeers() );

        //remove already participating peers
        newPeers.removeAll( environment.getPeers() );

        if ( newPeers.isEmpty() )
        {
            return;
        }

        //obtain reserved net resources
        final Map<Peer, UsedNetworkResources> reservedNetResources = Maps.newConcurrentMap();

        PeerUtil<Object> netQueryUtil = new PeerUtil<>();

        for ( final Peer peer : newPeers )
        {
            netQueryUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    reservedNetResources.put( peer, peer.getUsedNetworkResources() );

                    return null;
                }
            } ) );
        }

        Set<PeerUtil.PeerTaskResult<Object>> netQueryResults = netQueryUtil.executeParallel();

        boolean hasFailures = false;

        for ( PeerUtil.PeerTaskResult netQueryResult : netQueryResults )
        {
            if ( netQueryResult.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Obtained reserved network resources from peer %s",
                        netQueryResult.getPeer().getName() ) );
            }
            else
            {
                hasFailures = true;

                trackerOperation.addLog(
                        String.format( "Failed to obtain reserved network resources from peer %s. Reason: %s",
                                netQueryResult.getPeer().getName(), netQueryResult.getFailureReason() ) );
            }
        }

        if ( hasFailures )
        {
            throw new EnvironmentModificationException( "Failed to obtain reserved network resources from all peers" );
        }

        //check availability of network resources
        SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
        final String containerSubnet = subnetUtils.getInfo().getNetworkAddress();

        for ( Map.Entry<Peer, UsedNetworkResources> peerReservedNetResourcesEntry : reservedNetResources.entrySet() )
        {
            Peer peer = peerReservedNetResourcesEntry.getKey();
            UsedNetworkResources netResources = peerReservedNetResourcesEntry.getValue();

            if ( netResources.containerSubnetExists( containerSubnet ) )
            {
                throw new EnvironmentModificationException(
                        String.format( "Container subnet %s is already used on peer %s", environment.getSubnetCidr(),
                                peer.getName() ) );
            }
            if ( netResources.p2pSubnetExists( environment.getP2pSubnet() ) )
            {
                throw new EnvironmentModificationException(
                        String.format( "P2P subnet %s is already used on peer %s", environment.getP2pSubnet(),
                                peer.getName() ) );
            }
            if ( netResources.vniExists( environment.getVni() ) )
            {
                throw new EnvironmentModificationException(
                        String.format( "Vni %d is already used on peer %s", environment.getVni(), peer.getName() ) );
            }
        }

        //reserve network resources
        PeerUtil<Object> netReservationUtil = new PeerUtil<>();

        for ( final Peer peer : newPeers )
        {
            netReservationUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.reserveNetworkResource( new NetworkResourceImpl( environment.getId(), environment.getVni(),
                            environment.getP2pSubnet(), containerSubnet ) );
                    return null;
                }
            } ) );
        }

        Set<PeerUtil.PeerTaskResult<Object>> netReservationResults = netReservationUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult netReservationResult : netReservationResults )
        {
            if ( netReservationResult.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Reserved network resources on peer %s",
                        netReservationResult.getPeer().getName() ) );

                environment.addEnvironmentPeer( new PeerConfImpl( netReservationResult.getPeer().getId() ) );
            }
            else
            {
                hasFailures = true;

                trackerOperation.addLog( String.format( "Failed to reserve network resources on peer %s. Reason: %s",
                        netReservationResult.getPeer().getName(), netReservationResult.getFailureReason() ) );
            }
        }

        if ( hasFailures )
        {
            throw new EnvironmentModificationException( "Failed to reserve network resources on all peers" );
        }
    }
}
