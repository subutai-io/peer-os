package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.PeerManager;


/**
 * VNI setup generation
 *
 * TODO we need to add peer to environment metadata in this step (create/VniSetupStep and modify/VniSetupStep) rather
 * then in create/SetupP2PStep or modify/SetupP2PStep so that if env creation fails we can cleanup also reserved network
 * resources even if not actual creation took place
 */
public class VNISetupStep
{
    private static final Logger LOG = LoggerFactory.getLogger( VNISetupStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final TrackerOperation trackerOperation;


    public VNISetupStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                         final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentCreationException, PeerException
    {
        LOG.debug( "VNI setup started..." );

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        peers.add( peerManager.getLocalPeer() );

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        //obtain reserved gateways
        final Map<Peer, Gateways> reservedGateways = Maps.newConcurrentMap();
        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    reservedGateways.put( peer, peer.getGateways() );
                    return peer;
                }
            } );
        }

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.error( "Problems obtaining reserved gateways", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation
                    .addLog( String.format( "Obtained reserved gateways from peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation
                    .addLog( String.format( "Failed to obtain reserved gateways from peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentCreationException( "Failed to obtain reserved gateways from all peers" );
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

        LOG.debug( "Find free VNI..." );
        //calculate new vni
        long freeVni = findFreeVni( peers );

        //TODO: add gateway & p2p IP to reserve vni
        final Vni newVni = new Vni( freeVni, environment.getId() );

        //reserve new vni
        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.reserveVni( newVni );
                    return peer;
                }
            } );
        }

        succeededPeers.clear();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.error( "Problems reserving VNI", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Reserved VNI on peer %s", succeededPeer.getName() ) );
        }

        failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog( String.format( "Failed to reserve VNI on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentCreationException( "Failed to reserve VNI on all peers" );
        }

        //store vni in environment metadata
        environment.setVni( freeVni );
    }


    public long findFreeVni( final Set<Peer> peers ) throws EnvironmentCreationException, PeerException
    {

        final Set<Long> reservedVnis = Sets.newConcurrentHashSet();
        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    for ( Vni vni : peer.getReservedVnis().list() )
                    {
                        reservedVnis.add( vni.getVni() );
                    }
                    return peer;
                }
            } );
        }

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.error( "Problems getting reserved vnis", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Obtained reserved vnis from peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation
                    .addLog( String.format( "Failed to obtain reserved vnis from peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentCreationException( "Failed to obtain reserved vnis from all peers" );
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
