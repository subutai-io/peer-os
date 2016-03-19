package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.ArrayList;
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
import org.apache.commons.net.util.SubnetUtils;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.PeerManager;


public class SetupP2PStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetupP2PStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;


    public SetupP2PStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
    }


    public void execute() throws EnvironmentManagerException, InterruptedException, ExecutionException, PeerException
    {
        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );
        SubnetUtils.SubnetInfo info =
                new SubnetUtils( environment.getTunnelNetwork(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        String sharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );
        final String[] addresses = info.getAllAddresses();
        int counter = environment.getPeerConfs().size();


        ExecutorService p2pExecutor = Executors.newFixedThreadPool( peers.size() );

        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        List<P2PConfig> result = new ArrayList<>( peers.size() );

        for ( Peer peer : peers )
        {
            P2PConfig config = new P2PConfig( peer.getId(), environment.getId(), environment.getTunnelInterfaceName(),
                    environment.getTunnelCommunityName(), addresses[counter], sharedKey,
                    Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

            p2pCompletionService.submit( new SetupP2PConnectionTask( peer, config ) );

            counter++;
        }


        for ( Peer peer : peers )
        {
            final Future<P2PConfig> f = p2pCompletionService.take();
            P2PConfig config = f.get();
            result.add( config );
        }

        p2pExecutor.shutdown();

        for ( P2PConfig config : result )
        {
            environment.addEnvironmentPeer( new PeerConfImpl( config ) );
        }


        // tunnel setup

        Map<String, String> tunnels = environment.getTunnels();
        int peersCount = environment.getPeerConfs().size();
        ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peersCount );

        ExecutorCompletionService<Integer> tunnelCompletionService =
                new ExecutorCompletionService<Integer>( tunnelExecutor );

        for ( Peer peer : environment.getPeers() )
        {
            tunnelCompletionService.submit( new SetupTunnelTask( peer, environment.getId(), tunnels ) );
        }

        for ( Peer peer : environment.getPeers() )
        {
            final Future<Integer> f = tunnelCompletionService.take();
            Integer vlanId = f.get();
            LOGGER.debug( String.format( "VLAN_ID: %d", vlanId ) );
        }

        tunnelExecutor.shutdown();
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