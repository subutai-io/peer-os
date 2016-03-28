package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Objects;
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
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.PeerManager;


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


    public void execute() throws EnvironmentModificationException, PeerException
    {

        Set<Peer> newPeers = peerManager.resolve( topology.getAllPeers() );
        //remove already participating peers
        newPeers.removeAll( environment.getPeers() );
        newPeers.remove( peerManager.getLocalPeer() );

        if ( newPeers.isEmpty() )
        {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool( newPeers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        //obtain reserved gateways
        final Map<Peer, Gateways> reservedGateways = Maps.newConcurrentMap();
        for ( final Peer peer : newPeers )
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
        for ( Peer ignored : newPeers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems obtaining reserved gateways", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation
                    .addLog( String.format( "Obtained reserved gateways from peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( newPeers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation
                    .addLog( String.format( "Failed to obtain reserved gateways from peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentModificationException( "Failed to obtain reserved gateways from all peers" );
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
        final Vni environmentVni = new Vni( environment.getVni(), environment.getId() );

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
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.reserveVni( environmentVni );
                    return peer;
                }
            } );
        }

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
                LOG.error( "Problems reserving VNI", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Reserved VNI on peer %s", succeededPeer.getName() ) );
        }

        failedPeers = Sets.newHashSet( newPeers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog( String.format( "Failed to reserve VNI on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentModificationException( "Failed to reserve VNI on all peers" );
        }
    }
}
