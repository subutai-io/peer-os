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
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.EnvironmentPeerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;


public class ReservationStep
{

    private final Topology topology;
    private final LocalEnvironment environment;
    private final PeerManager peerManager;
    private final IdentityManager identityManager;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();


    public ReservationStep( final Topology topology, final LocalEnvironment environment, final PeerManager peerManager,
                            final IdentityManager identityManager, final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.identityManager = identityManager;
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
        final Map<Peer, UsedNetworkResources> reservedNetResources = obtainReservedNetResources( newPeers );


        //check availability of network resources
        SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
        final String containerSubnet = subnetUtils.getInfo().getNetworkAddress();

        checkResourceAvailablility( reservedNetResources, containerSubnet );

        //reserve network resources
        reserveNetworkResources( newPeers, containerSubnet );
    }


    void checkResourceAvailablility( Map<Peer, UsedNetworkResources> reservedNetResources, String containerSubnet )
            throws EnvironmentModificationException
    {
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
    }


    private void reserveNetworkResources( Set<Peer> newPeers, final String containerSubnet )
            throws EnvironmentModificationException
    {

        for ( final Peer peer : newPeers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Integer call() throws Exception
                {
                    NetworkResourceImpl networkResource =
                            new NetworkResourceImpl( environment.getId(), environment.getVni(),
                                    environment.getP2pSubnet(), containerSubnet, peerManager.getLocalPeer().getId(),
                                    identityManager.getActiveUser().getUserName() );

                    return peer.reserveNetworkResource( networkResource );
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> netReservationResults = peerUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult<Object> netReservationResult : netReservationResults.getResults() )
        {
            if ( netReservationResult.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Reserved network resources on peer %s",
                        netReservationResult.getPeer().getName() ) );

                environment.addEnvironmentPeer( new EnvironmentPeerImpl( netReservationResult.getPeer().getId(),
                        ( Integer ) netReservationResult.getResult() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Failed to reserve network resources on peer %s. Reason: %s",
                        netReservationResult.getPeer().getName(), netReservationResult.getFailureReason() ) );
            }
        }

        if ( netReservationResults.hasFailures() )
        {
            throw new EnvironmentModificationException( "Failed to reserve network resources on all peers" );
        }
    }


    private Map<Peer, UsedNetworkResources> obtainReservedNetResources( Set<Peer> newPeers )
            throws EnvironmentModificationException
    {
        final Map<Peer, UsedNetworkResources> reservedNetResources = Maps.newConcurrentMap();


        for ( final Peer peer : newPeers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    reservedNetResources.put( peer, peer.getUsedNetworkResources() );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> netQueryResults = peerUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult netQueryResult : netQueryResults.getResults() )
        {
            if ( netQueryResult.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Obtained reserved network resources from peer %s",
                        netQueryResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog(
                        String.format( "Failed to obtain reserved network resources from peer %s. Reason: %s",
                                netQueryResult.getPeer().getName(), netQueryResult.getFailureReason() ) );
            }
        }

        if ( netQueryResults.hasFailures() )
        {
            throw new EnvironmentModificationException( "Failed to obtain reserved network resources from all peers" );
        }

        return reservedNetResources;
    }
}
