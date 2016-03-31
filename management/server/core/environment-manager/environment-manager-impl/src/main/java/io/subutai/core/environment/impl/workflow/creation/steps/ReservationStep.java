package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.PeerManager;


/**
 * Network resources reservation step
 */
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


    public void execute() throws EnvironmentCreationException, PeerException
    {
        LOG.debug( "Network resource reservation step started..." );

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        peers.add( peerManager.getLocalPeer() );

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        //obtain reserved net resources
        final Map<Peer, UsedNetworkResources> reservedNetResources = Maps.newConcurrentMap();
        for ( final Peer peer : peers )
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
        for ( Peer ignored : peers )
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

        Set<Peer> failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog(
                    String.format( "Failed to obtain reserved network resources from peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
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
        final String freeContainerSubnet = P2PUtil.findFreeContainerSubnet( allContainerSubnets );

        if ( freeContainerSubnet == null )
        {
            throw new EnvironmentCreationException( "Free container subnet not found" );
        }

        //calculate free p2p subnet
        final String freeP2pSubnet = P2PUtil.findFreeP2PSubnet( allP2pSubnets );

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
        executorService = Executors.newFixedThreadPool( peers.size() );
        completionService = new ExecutorCompletionService<>( executorService );

        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.reserveNetworkResource( new NetworkResourceImpl( environment.getId(), freeVni, freeP2pSubnet,
                            freeContainerSubnet ) );
                    return peer;
                }
            } );
        }

        executorService.shutdown();

        succeededPeers.clear();
        for ( Peer ignored : peers )
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

        failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation
                    .addLog( String.format( "Failed to reserve network resources on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
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
