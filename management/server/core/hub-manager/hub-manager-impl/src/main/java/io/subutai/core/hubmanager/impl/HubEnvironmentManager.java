package io.subutai.core.hubmanager.impl;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.dto.PublicKeyContainer;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class HubEnvironmentManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentManager.class.getName() );
    private static final String MANAGEMENT_HOST_NETWORK_BINDING = "subutai management_network";
    private static final String MANAGEMENT_PROXY_BINDING = "subutai proxy";
    private static final String SSH_FOLDER = "/root/.ssh";
    private static final String SSH_FILE = String.format( "%s/authorized_keys", SSH_FOLDER );

    private SecurityManager securityManager;
    private PeerManager peerManager;
    private ConfigManager configManager;
    private IdentityManager identityManager;
    private EnvironmentManager environmentManager;
    private NetworkManager networkManager;


    public HubEnvironmentManager( final EnvironmentManager environmentManager, final ConfigManager hConfigManager,
                                  final PeerManager peerManager, final IdentityManager identityManager,
                                  final NetworkManager networkManager )
    {
        this.environmentManager = environmentManager;
        this.configManager = hConfigManager;
        this.peerManager = peerManager;
        this.identityManager = identityManager;
        this.networkManager = networkManager;
    }


    public Set<Long> getReservedVnis()
    {
        Set<Long> vniDtos = new HashSet<>();
        try
        {
            Vnis vnis = peerManager.getLocalPeer().getReservedVnis();
            for ( Vni vni : vnis.list() )
            {
                vniDtos.add( vni.getVni() );
            }
            return vniDtos;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not get local peer reserved vnis" );
        }
        return null;
    }


    public Set<String> getTunnelNetworks()
    {
        Set<String> usedInterfaces = new HashSet<>();
        try
        {
            Set<HostInterfaceModel> r =
                    peerManager.getLocalPeer().getInterfaces().filterByIp( P2PUtil.P2P_INTERFACE_IP_PATTERN );


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

            usedInterfaces.addAll( tunnels );
            return usedInterfaces;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not get local peer used interfaces" );
        }
        return null;
    }


    public Set<String> getReservedGateways()
    {
        Set<String> gatewayDtos = new HashSet<>();
        try
        {
            Gateways gateways = peerManager.getLocalPeer().getGateways();
            for ( Gateway gateway : gateways.list() )
            {
                gatewayDtos.add( gateway.getIp() );
            }
            return gatewayDtos;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not get local peer used interfaces" );
        }
        return null;
    }


    public PublicKeyContainer createPeerEnvironmentKeyPair( EnvironmentId environmentId )
    {
        try
        {
            io.subutai.common.security.PublicKeyContainer publicKeyContainer =
                    peerManager.getLocalPeer().createPeerEnvironmentKeyPair( environmentId );

            PublicKeyContainer keyContainer = new PublicKeyContainer();
            keyContainer.setKey( publicKeyContainer.getKey() );
            keyContainer.setHostId( publicKeyContainer.getHostId() );
            keyContainer.setFingerprint( publicKeyContainer.getFingerprint() );

            return keyContainer;
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not create local peer PEK" );
        }
        return null;
    }


    public void setupVNI( EnvironmentPeerDto peerDto )
    {
        try
        {
            Vni vni = new Vni( peerDto.getEnvironmentInfo().getVni(), peerDto.getEnvironmentInfo().getId() );
            peerManager.getLocalPeer().reserveVni( vni );
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not setup VNI" );
        }
    }


    public EnvironmentPeerDto setupP2P( EnvironmentPeerDto peerDto )
    {
        LocalPeer peer = peerManager.getLocalPeer();
        final EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        String tunComName = P2PUtil.generateHash( env.getTunnelNetwork() );

        SubnetUtils.SubnetInfo subnetInfo =
                new SubnetUtils( peerDto.getEnvironmentInfo().getTunnelNetwork(), P2PUtil.P2P_SUBNET_MASK ).getInfo();
        final String address = subnetInfo.getAddress();
        final Session session = identityManager.login( "internal", "secretSubutai" );
//        Subject.doAs( session.getSubject(), new PrivilegedAction<Void>()
//        {
//            @Override
//            public Void run()
//            {
                try
                {
                    Vni reservedVni = networkManager.getReservedVnis().findVniByEnvironmentId( env.getId() );
                    networkManager.setupP2PConnection( peerManager.getLocalPeer().getManagementHost(),
                            P2PUtil.generateInterfaceName( reservedVni.getVlan() ), address,
                            env.getP2pHash(), env.getP2pKey(), Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );
                }
                catch ( Exception ex )
                {
                    LOG.error( ex.getMessage() );
                }
//                return null;
//            }
//        } );

        ExecutorService p2pExecutor = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        P2PConfig config =
                new P2PConfig( peer.getId(), env.getId(), env.getP2pKey(), address, env.getP2pHash(), env.getP2pTTL() );
        p2pCompletionService.submit( new SetupP2PConnectionTask( peer, config ) );

        try
        {
            final Future<P2PConfig> f = p2pCompletionService.take();
            P2PConfig createdConfig = f.get();
            p2pExecutor.shutdown();

            peerDto.setTunnelAddress( createdConfig.getAddress() );
            peerDto.setCommunityName( createdConfig.getHash() );
            peerDto.setP2pSecretKey( createdConfig.getSecretKey() );

            ExecutorService tunnelExecutor = Executors.newSingleThreadExecutor();

            ExecutorCompletionService<Integer> tunnelCompletionService =
                    new ExecutorCompletionService<Integer>( tunnelExecutor );

            Map<String, String> tunnel = new HashMap<>();
            tunnel.put( peerDto.getPeerId(), peerDto.getTunnelAddress() );
            tunnelCompletionService.submit( new SetupTunnelTask( peer, env.getId(), tunnel ) );

            final Future<Integer> fTunnel = tunnelCompletionService.take();
            fTunnel.get();

            tunnelExecutor.shutdown();
        }
        catch ( Exception e )
        {
            LOG.error( "Could not create P2P tunnel.", e.getMessage() );
        }

        return peerDto;
    }


    public void prepareTemplates( EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto )
    {
        Set<Node> nodes = new HashSet<>();
        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
            Node node =
                    new Node( nodeDto.getHostName(), nodeDto.getContainerName(), nodeDto.getTemplateName(), contSize, 0,
                            0, peerDto.getPeerId(), nodeDto.getHostId() );
            nodes.add( node );
        }

        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
        CompletionService<PrepareTemplatesResponseCollector> taskCompletionService =
                getCompletionService( taskExecutor );

        LOG.debug( String.format( "Preparing templates on peer %s", peerManager.getLocalPeer().getId() ) );
        taskCompletionService.submit( new CreatePeerTemplatePrepareTask( peerManager.getLocalPeer(), nodes ) );

        taskExecutor.shutdown();

        try
        {
            Future<PrepareTemplatesResponseCollector> futures = taskCompletionService.take();
            final PrepareTemplatesResponseCollector prepareTemplatesResponse = futures.get();

            if ( !prepareTemplatesResponse.hasSucceeded() )
            {
                LOG.error( "There were errors during preparation of templates on peer " + prepareTemplatesResponse
                        .getPeerId() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "There were errors during preparation templates. Unexpected error.", e.getMessage() );
        }
    }


    public EnvironmentNodesDto cloneContainers( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes )
    {
        try
        {
            CreateEnvironmentContainerGroupRequest containerGroupRequest =
                    new CreateEnvironmentContainerGroupRequest( peerDto.getEnvironmentInfo().getId() );

            for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
            {
                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
                try
                {
                    CloneRequest cloneRequest =
                            new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                                    nodeDto.getIp(), peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId(),
                                    peerDto.getOwnerId(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );

                    containerGroupRequest.addRequest( cloneRequest );
                }
                catch ( Exception e )
                {
                    LOG.error( "Could not create container clone request", e.getMessage() );
                }
            }

            final CreateEnvironmentContainerResponseCollector containerCollector =
                    peerManager.getLocalPeer().createEnvironmentContainerGroup( containerGroupRequest );

            List<CloneResponse> cloneResponseList = containerCollector.getResponses();
            for ( CloneResponse cloneResponse : cloneResponseList )
            {
                for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
                {
                    if ( cloneResponse.getHostname().equals( nodeDto.getHostName() ) )
                    {
                        nodeDto.setIp( cloneResponse.getIp() );
                        nodeDto.setTemplateArch( cloneResponse.getTemplateArch().name() );
                        nodeDto.setContainerId( cloneResponse.getContainerId() );
                        nodeDto.setElapsedTime( cloneResponse.getElapsedTime() );
                    }
                }
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not clone containers" );
        }
        return envNodes;
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


    protected CompletionService<PrepareTemplatesResponseCollector> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }


    public RequestBuilder getSetupP2PConnectionCommand( String interfaceName, String localIp, String p2pHash,
                                                        String secretKey, long secretKeyTtlSec )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "p2p", "-c", interfaceName, p2pHash, secretKey, String.valueOf( secretKeyTtlSec ),
                        Strings.isNullOrEmpty( localIp ) ? "" : localIp ) ).withTimeout( 90 );
    }
}
