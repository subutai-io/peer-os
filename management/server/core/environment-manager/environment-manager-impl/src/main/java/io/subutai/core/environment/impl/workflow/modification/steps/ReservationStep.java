package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.PeerManager;


public class ReservationStep
{
    private static final Logger LOG = LoggerFactory.getLogger( ReservationStep.class );

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

        ExecutorService executorService = Executors.newFixedThreadPool( newPeers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        //obtain reserved net resources
        final Map<Peer, UsedNetworkResources> reservedNetResources = Maps.newConcurrentMap();
        for ( final Peer peer : newPeers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    reservedNetResources.put( peer, peer.getUsedNetworkResources() );
                    return peer;
                }
            } );
        }

        executorService.shutdown();

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : newPeers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems obtaining reserved network resources", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog(
                    String.format( "Obtained reserved network resources from peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( newPeers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog(
                    String.format( "Failed to obtain reserved network resources from peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
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
        executorService = Executors.newFixedThreadPool( newPeers.size() );
        completionService = new ExecutorCompletionService<>( executorService );

        for ( final Peer peer : newPeers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.reserveNetworkResource( new NetworkResourceImpl( environment.getId(), environment.getVni(),
                            environment.getP2pSubnet(), containerSubnet ) );
                    return peer;
                }
            } );
        }

        executorService.shutdown();

        succeededPeers.clear();
        for ( Peer ignored : newPeers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems reserving network resources", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation
                    .addLog( String.format( "Reserved network resources on peer %s", succeededPeer.getName() ) );

            environment.addEnvironmentPeer( new PeerConfImpl( succeededPeer.getId() ) );
        }

        failedPeers = Sets.newHashSet( newPeers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation
                    .addLog( String.format( "Failed to reserve network resources on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentModificationException( "Failed to reserve network resources on all peers" );
        }
    }
}
