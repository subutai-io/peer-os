package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;


/**
 * P2P setup step
 */
public class SetupP2PStep
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupP2PStep.class );

    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final NetworkManager networkManager;
    private final TrackerOperation trackerOperation;


    public SetupP2PStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                         final NetworkManager networkManager, final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentCreationException, PeerException, NetworkManagerException
    {

        //obtain participating peers
        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        //add local peer
        peers.add( peerManager.getLocalPeer() );

        // figure out free p2p subnet
        Set<String> usedSubnets = getUsedP2PSubnets( peers );
        String freeP2pSubnet = P2PUtil.findFreeSubnet( usedSubnets );
        LOG.debug( String.format( "Free p2p subnet: %s", freeP2pSubnet ) );
        if ( freeP2pSubnet == null )
        {
            throw new EnvironmentCreationException( "Free p2p subnet not found" );
        }

        environment.setP2PSubnet( freeP2pSubnet );

        SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeP2pSubnet, P2PUtil.P2P_SUBNET_MASK ).getInfo();
        String sharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );
        final String[] addresses = subnetInfo.getAllAddresses();
        Vni reservedVni =
                networkManager.getReservedVnis().findVniByEnvironmentId( environment.getEnvironmentId().getId() );

        //setup initial p2p participant on local peer MH with explicit IP
        networkManager.setupP2PConnection( peerManager.getLocalPeer().getManagementHost(),
                P2PUtil.generateInterfaceName( reservedVni.getVlan() ), addresses[0], environment.getP2PHash(),
                sharedKey, Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        ExecutorService p2pExecutor = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        int counter = 1;
        for ( Peer peer : peers )
        {
            P2PConfig config =
                    new P2PConfig( peer.getId(), environment.getId(), environment.getP2PHash(), addresses[counter],
                            sharedKey, Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );
            p2pCompletionService.submit( new SetupP2PConnectionTask( peer, config ) );
            counter++;
        }

        // p2p setup
        List<P2PConfig> result = new ArrayList<>( peers.size() );
        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer peer : peers )
        {
            try
            {
                final Future<P2PConfig> f = p2pCompletionService.take();
                P2PConfig config = f.get();
                result.add( config );
                succeededPeers.add( peer );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.error( "Problems setting up p2p connection", e );
            }
        }

        p2pExecutor.shutdown();

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "P2P setup succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "P2P setup failed on peer %s", failedPeer.getName() ) );
        }

        for ( P2PConfig config : result )
        {
            environment.addEnvironmentPeer( new PeerConfImpl( config ) );
        }

        if ( !peers.isEmpty() )
        {
            throw new EnvironmentCreationException( "Failed to setup P2P connection across all peers" );
        }

        //tunnel setup

        Map<String, String> tunnels = environment.getTunnels();
        int peersCount = environment.getPeerConfs().size();
        ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peersCount );
        ExecutorCompletionService<Integer> tunnelCompletionService =
                new ExecutorCompletionService<Integer>( tunnelExecutor );

        for ( Peer peer : environment.getPeers() )
        {
            tunnelCompletionService.submit( new SetupTunnelTask( peer, environment.getId(), tunnels ) );
        }

        succeededPeers = Sets.newHashSet();
        peers = environment.getPeers();

        for ( Peer peer : peers )
        {
            final Future<Integer> f;
            try
            {
                f = tunnelCompletionService.take();
                f.get();
                succeededPeers.add( peer );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.error( "Problems setting up tunnels", e );
            }
        }

        tunnelExecutor.shutdown();

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Tunnel setup succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "Tunnel setup failed on peer %s", failedPeer.getName() ) );
        }

        if ( !peers.isEmpty() )
        {
            throw new EnvironmentCreationException( "Failed to setup tunnel across all peers" );
        }
    }


    private Set<String> getUsedP2PSubnets( final Set<Peer> peers ) throws PeerException
    {
        Set<String> result = new HashSet<>();

        for ( Peer peer : peers )
        {
            Set<HostInterfaceModel> r = peer.getInterfaces().filterByIp( P2PUtil.P2P_INTERFACE_IP_PATTERN );

            Collection tunnels = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    HostInterface i = ( HostInterface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), P2PUtil.P2P_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            result.addAll( tunnels );
        }

        return result;
    }


    private class SetupP2PConnectionTask implements Callable<P2PConfig>
    {
        private Peer peer;
        private P2PConfig p2PConfig;


        public SetupP2PConnectionTask( final Peer peer, final P2PConfig config )
        {
            this.peer = peer;
            this.p2PConfig = config;
        }


        @Override
        public P2PConfig call() throws Exception
        {
            p2PConfig.setAddress( peer.setupP2PConnection( p2PConfig ) );
            return p2PConfig;
        }
    }


    private class SetupTunnelTask implements Callable<Integer>
    {
        private final Peer peer;
        private final String environmentId;
        private final Map<String, String> tunnels;


        public SetupTunnelTask( final Peer peer, final String environmentId, final Map<String, String> tunnels )
        {
            this.peer = peer;
            this.environmentId = environmentId;
            this.tunnels = tunnels;
        }


        @Override
        public Integer call() throws Exception
        {
            return peer.setupTunnels( tunnels, environmentId );
        }
    }
}
