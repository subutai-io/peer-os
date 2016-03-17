package io.subutai.core.hubmanager.impl.environment;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.task.ImportTemplateResponse;
import io.subutai.common.tracker.OperationMessage;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentBuilder
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private IntegrationImpl manager;

    private ConfigManager configManager;

    private PeerManager peerManager;

    public EnvironmentBuilder( IntegrationImpl integration, ConfigManager configManager, PeerManager peerManager )
    {
        this.manager = integration;
        this.configManager = configManager;
        this.peerManager = peerManager;
    }


    private long vniId = 1234560;

//    private String envId = UUID.randomUUID().toString();
    private String envId = "f8f3e4de-2bf9-45e6-98f4-f09d65a86723"; // Should be UUID. Otherwise reserving VNI doesn't work.

    private String p2pSubnet = "10.11.12.";

    private String sharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );

    public void build() throws Exception
    {
//        buildVni();

//        p2p();

        prepareTemplates();
    }


    private void buildVni() throws Exception
    {
        Vni vni = new Vni( vniId, envId );

        Vni resultVni = peerManager.getLocalPeer().reserveVni( vni );

        log.info( "resultVni: {}", resultVni );

        log.info( "reserved vnis: {}", peerManager.getLocalPeer().getReservedVnis().list() );
    }


    // -----------------------------------------------------------------------------------------------------------------------------------------------
    // prepare templates
    // -----------------------------------------------------------------------------------------------------------------------------------------------


    private void prepareTemplates() throws InterruptedException, ExecutionException
    {
        String hostname = UUID.randomUUID().toString();

        Node node = new Node( hostname, "Container Name", "master", ContainerSize.SMALL, 0, 0, peerManager.getLocalPeer().getId(), getFirstResourceHostId() );

        Set<Node> nodes = Sets.newHashSet( node );

        ExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        CompletionService<PrepareTemplatesResponseCollector> taskCompletionService = getCompletionService( exec );

        taskCompletionService.submit( new CreatePeerTemplatePrepareTask( peerManager.getLocalPeer(), nodes ) );

        exec.shutdown();

        Future<PrepareTemplatesResponseCollector> future = taskCompletionService.take();

        PrepareTemplatesResponseCollector response = future.get();

        for ( ImportTemplateResponse importTemplateResponse : response.getResponses() )
        {
            // ImportTemplateResponse{resourceHostId='A57DBD6CF41B4A33F97686BD5E4B238A96210297', templateName='master', elapsedTime=408}
            log.debug( "{}", importTemplateResponse );
        }

        log.debug( "Operation messages:" );

        for ( OperationMessage message : response.getOperationMessages() )
        {
            log.debug( message.getDescription() );
        }
    }


    private String getFirstResourceHostId()
    {
        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            return rh.getId();
        }

        return null;
    }


    private CompletionService<PrepareTemplatesResponseCollector> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }


    // -----------------------------------------------------------------------------------------------------------------------------------------------
    // p2p
    // -----------------------------------------------------------------------------------------------------------------------------------------------


    private void p2p() throws Exception
    {
//        P2PConfig config = new P2PConfig(
//                peerManager.getLocalPeer().getId(),
//                envId,
//                "p2p_" + p2pSubnet + "0",
//                "com_" + envId,
//                  p2pSubnet + "1",
//                sharedKey,
//                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );
//
//        ExecutorService p2pExecutor = Executors.newSingleThreadScheduledExecutor();
//
//        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );
//
//        p2pCompletionService.submit( new SetupP2PConnectionTask( peerManager.getLocalPeer(), config ) );
//
//        Future<P2PConfig> f = p2pCompletionService.take();
//
//        f.get();
//
//        p2pExecutor.shutdown();

        //
        // Setup tunnels
        //

//        Map<String, String> tunnels = ImmutableMap.of( "89D869D0AB3C1064E7275CF0F716EDFB0B6E56AA", p2pSubnet + "1" );
        Map<String, String> tunnels = ImmutableMap.of( peerManager.getLocalPeer().getId(), p2pSubnet + "1" );

        ExecutorService tunnelExecutor = Executors.newSingleThreadScheduledExecutor();

        ExecutorCompletionService<Integer> tunnelCompletionService = new ExecutorCompletionService( tunnelExecutor );

        tunnelCompletionService.submit( new SetupTunnelTask( peerManager.getLocalPeer(), envId, tunnels ) );

        Future<Integer> f2 = tunnelCompletionService.take();

        f2.get();

        tunnelExecutor.shutdown();
    }


    // Returns already used subnets for p2p: 10.x.x.x
    private Set<String> getTunnelNetworks() throws PeerException
    {
        Set<String> result = new HashSet<>();

        Set<HostInterfaceModel> r = peerManager.getLocalPeer().getInterfaces().filterByIp( P2PUtil.P2P_INTERFACE_IP_PATTERN );

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

        return result;
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


    private class SetupP2PConnectionTask implements Callable<P2PConfig>
    {
        private Peer peer;
        private P2PConfig p2PConfig;


        public SetupP2PConnectionTask( Peer peer, P2PConfig config )
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
}
