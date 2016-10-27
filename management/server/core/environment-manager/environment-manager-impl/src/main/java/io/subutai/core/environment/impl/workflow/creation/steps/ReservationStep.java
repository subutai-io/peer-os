package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.EnvironmentPeerImpl;
import io.subutai.core.peer.api.PeerManager;


/**
 * Network resources reservation step
 * TODO refactor - split into smaller methods
 */
public class ReservationStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();


    public ReservationStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                            final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentCreationException, PeerException
    {

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        //obtain reserved net resources
        final Map<Peer, UsedNetworkResources> reservedNetResources = Maps.newConcurrentMap();


        for ( final Peer peer : peers )
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
            throw new EnvironmentCreationException( "Failed to obtain reserved network resources from all peers" );
        }

        Set<String> allP2pSubnets = Sets.newHashSet();
        Set<String> allContainerSubnets = Sets.newHashSet();
        Set<Long> allVnis = Sets.newHashSet();

        for ( UsedNetworkResources netResources : reservedNetResources.values() )
        {
            allContainerSubnets.addAll( netResources.getContainerSubnets() );
            allP2pSubnets.addAll( netResources.getP2pSubnets() );
            allVnis.addAll( netResources.getVnis() );
        }

        //calculate free container subnet
        final String freeContainerSubnet = P2PUtil.generateContainerSubnet( allContainerSubnets );

        if ( freeContainerSubnet == null )
        {
            throw new EnvironmentCreationException( "Free container subnet not found" );
        }

        //calculate free p2p subnet
        final String freeP2pSubnet = P2PUtil.generateP2PSubnet( allP2pSubnets );

        if ( freeP2pSubnet == null )
        {
            throw new EnvironmentCreationException( "Free p2p subnet not found" );
        }

        //calculate free vni
        final Long freeVni = generateRandomVni( allVnis );

        if ( freeVni == -1 )
        {
            throw new EnvironmentCreationException( "Free VNI not found" );
        }

        //reserve network resources

        for ( final Peer peer : peers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Integer call() throws Exception
                {
                    return peer.reserveNetworkResource(
                            new NetworkResourceImpl( environment.getId(), freeVni, freeP2pSubnet,
                                    freeContainerSubnet ) );
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
            throw new EnvironmentCreationException( "Failed to reserve network resources on all peers" );
        }

        //store network data in environment metadata
        environment.setVni( freeVni );
        environment.setP2PSubnet( freeP2pSubnet );
        environment.setSubnetCidr( String.format( "%s/24", freeContainerSubnet ) );
    }


    protected long generateRandomVni( Set<Long> excludedVnis )
    {
        int maxIterations = 10000;
        int currentIteration = 0;
        long vni;

        do
        {
            vni = ( long ) ( Math.random() * ( Common.MAX_VNI_ID - Common.MIN_VNI_ID ) ) + Common.MIN_VNI_ID;
            currentIteration++;
        }
        while ( excludedVnis.contains( vni ) && currentIteration < maxIterations );

        if ( excludedVnis.contains( vni ) )
        {
            return -1;
        }

        return vni;
    }
}
