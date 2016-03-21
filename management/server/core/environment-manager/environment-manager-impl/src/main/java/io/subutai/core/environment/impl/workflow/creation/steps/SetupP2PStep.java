package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
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

import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;


/**
 * P2P setup step
 */
public class SetupP2PStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetupP2PStep.class );

    private final Topology topology;
    private final EnvironmentImpl env;
    private final PeerManager peerManager;
    private final NetworkManager networkManager;


    public SetupP2PStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager,
                         final NetworkManager networkManager )
    {
        this.topology = topology;
        this.env = environment;
        this.peerManager = peerManager;
        this.networkManager = networkManager;
    }


    public void execute() throws EnvironmentManagerException
    {
        try
        {
            //obtain already participating peers
            Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

            peers.add( peerManager.getLocalPeer() );
            // creating new p2p tunnels
            Set<String> existingNetworks = getTunnelNetworks( peers );

            String freeTunnelNetwork = P2PUtil.findFreeTunnelNetwork( existingNetworks );

            LOGGER.debug( String.format( "Free tunnel network: %s", freeTunnelNetwork ) );

            if ( freeTunnelNetwork == null )
            {
                throw new IllegalStateException( "Could not calculate tunnel network." );
            }

            env.setTunnelNetwork( freeTunnelNetwork );

            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeTunnelNetwork, P2PUtil.P2P_SUBNET_MASK ).getInfo();

            ExecutorService p2pExecutor = Executors.newFixedThreadPool( peers.size() );

            ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );


            String sharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );
            final String[] addresses = subnetInfo.getAllAddresses();

            //setup initial p2p participant local peer MH
            networkManager
                    .setupP2PConnection( peerManager.getLocalPeer().getManagementHost(), env.getTunnelInterfaceName(),
                            addresses[0], env.getTunnelCommunityName(), sharedKey,
                            Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

            int counter = 1;
            for ( Peer peer : peers )
            {
                P2PConfig config = new P2PConfig( peer.getId(), env.getId(), env.getTunnelInterfaceName(),
                        env.getTunnelCommunityName(), addresses[counter], sharedKey,
                        Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );
                p2pCompletionService.submit( new SetupP2PConnectionTask( peer, config ) );
                counter++;
            }

            // p2p setup
            List<P2PConfig> result = new ArrayList<>( peers.size() );
            for ( Peer peer : peers )
            {
                final Future<P2PConfig> f = p2pCompletionService.take();
                P2PConfig config = f.get();
                result.add( config );
            }

            p2pExecutor.shutdown();

            for ( P2PConfig config : result )
            {
                env.addEnvironmentPeer( new PeerConfImpl( config ) );
            }


            int peersCount = env.getPeerConfs().size();
            ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peersCount );

            ExecutorCompletionService<Integer> tunnelCompletionService =
                    new ExecutorCompletionService<Integer>( tunnelExecutor );

            // tunnel setup
            Map<String, String> tunnels = env.getTunnels();
            for ( Peer peer : env.getPeers() )
            {
                tunnelCompletionService.submit( new SetupTunnelTask( peer, env.getId(), tunnels ) );
            }

            for ( int i = 0; i < peersCount; i++ )
            {
                final Future<Integer> f = tunnelCompletionService.take();
                f.get();
            }

            tunnelExecutor.shutdown();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new EnvironmentManagerException( "Could not create P2P tunnel.", e );
        }
    }


    private Set<String> getTunnelNetworks( final Set<Peer> peers ) throws PeerException
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
            peer.setupP2PConnection( p2PConfig );
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
