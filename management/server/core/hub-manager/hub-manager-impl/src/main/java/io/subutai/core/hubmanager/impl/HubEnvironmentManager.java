package io.subutai.core.hubmanager.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.dto.PublicKeyContainer;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.SSHKeyDto;


public class HubEnvironmentManager
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentManager.class.getName() );
    private static final String MANAGEMENT_HOST_NETWORK_BINDING = "subutai management_network";
    private static final String MANAGEMENT_PROXY_BINDING = "subutai proxy";
    private static final String SSH_FOLDER = "/root/.ssh";
    private static final String SSH_FILE = String.format( "%s/authorized_keys", SSH_FOLDER );
    protected CommandUtil commandUtil = new CommandUtil();

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


    public EnvironmentPeerDto getReservedNetworkResource( EnvironmentPeerDto peerDto )
            throws EnvironmentCreationException
    {
        final Map<Peer, UsedNetworkResources> reservedNetResources = Maps.newConcurrentMap();
        final LocalPeer localPeer = peerManager.getLocalPeer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {
                reservedNetResources.put( localPeer, localPeer.getUsedNetworkResources() );
                return localPeer;
            }
        } );

        executorService.shutdown();
        try
        {
            Future<Peer> f = completionService.take();
            f.get();
        }
        catch ( Exception e )
        {
            throw new EnvironmentCreationException( "Failed to obtain reserved network resources from local peer" );
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
        peerDto.setVnis( allVnis );
        peerDto.setContainerSubnets( allContainerSubnets );
        peerDto.setP2pSubnets( allP2pSubnets );
        return peerDto;
    }


    public void reserveNetworkResource( EnvironmentPeerDto peerDto ) throws EnvironmentCreationException
    {
        final LocalPeer localPeer = peerManager.getLocalPeer();
        final EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final String subnetWithoutMask = env.getSubnetCidr().replace( "/24", "" );
        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {
                localPeer.reserveNetworkResource(
                        new NetworkResourceImpl( env.getId(), env.getVni(), env.getP2pSubnet(), subnetWithoutMask ) );
                return localPeer;
            }
        } );

        executorService.shutdown();
        try
        {
            Future<Peer> f = completionService.take();
            f.get();
        }
        catch ( Exception e )
        {
            throw new EnvironmentCreationException( "Failed to reserve network resources on all peers" );
        }
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


    public EnvironmentPeerDto setupP2P( EnvironmentPeerDto peerDto )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        SubnetUtils.SubnetInfo subnetInfo =
                new SubnetUtils( peerDto.getEnvironmentInfo().getP2pSubnet(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        final String[] addresses = subnetInfo.getAllAddresses();

        try
        {
            localPeer.createP2PSwarm(
                    new P2PConfig( localPeer.getId(), env.getId(), env.getP2pHash(), addresses[0], env.getP2pKey(),
                            env.getP2pTTL() ) );
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not setup initial p2p participant on local peer MH with explicit IP", e );
        }

        ExecutorService p2pExecutor = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        P2PConfig config = new P2PConfig( localPeer.getId(), env.getId(), env.getP2pHash(), null, env.getP2pKey(),
                env.getP2pTTL() );
        p2pCompletionService.submit( new SetupP2PConnectionTask( localPeer, config ) );

        try
        {

            final Future<P2PConfig> f = p2pCompletionService.take();
            P2PConfig createdConfig = f.get();
            p2pExecutor.shutdown();
            peerDto.setTunnelAddress( createdConfig.getAddress() );
            peerDto.setCommunityName( createdConfig.getHash() );
            peerDto.setP2pSecretKey( createdConfig.getSecretKey() );
        }
        catch ( ExecutionException | InterruptedException e )
        {
            LOG.error( "Problems setting up p2p connection", e );
        }
        return peerDto;
    }


    public void setupTunnel( EnvironmentDto environmentDto ) throws InterruptedException, ExecutionException
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        Set<String> setOfP2PIps = new HashSet<>();
        P2pIps p2pIps = new P2pIps();

        for ( EnvironmentPeerDto peerDto : environmentDto.getPeers() )
        {
            if ( !peerDto.getPeerId().equals( localPeer.getId() ) )
            {
                setOfP2PIps.add( peerDto.getTunnelAddress() );
            }
        }
        p2pIps.addP2pIps( setOfP2PIps );
        if ( !p2pIps.isEmpty() )
        {
            ExecutorService tunnelExecutor = Executors.newSingleThreadExecutor();
            ExecutorCompletionService<Boolean> tunnelCompletionService =
                    new ExecutorCompletionService<>( tunnelExecutor );

            tunnelCompletionService.submit( new SetupTunnelTask( localPeer, environmentDto.getId(), p2pIps ) );

            final Future<Boolean> f = tunnelCompletionService.take();
            f.get();

            tunnelExecutor.shutdown();
        }
    }


    public void prepareTemplates( EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto )
    {
        Set<Node> nodes = new HashSet<>();
        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
            {
                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
                Node node = new Node( nodeDto.getHostName(), nodeDto.getContainerName(), nodeDto.getTemplateName(),
                        contSize, 0, 0, peerDto.getPeerId(), nodeDto.getHostId() );
                nodes.add( node );
            }
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
                if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
                {
                    ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
                    try
                    {
                        nodeDto.setState( ContainerStateDto.UNKNOWN );
                        CloneRequest cloneRequest = new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(),
                                nodeDto.getContainerName(), nodeDto.getIp(), peerDto.getEnvironmentInfo().getId(),
                                peerDto.getPeerId(), peerDto.getOwnerId(), nodeDto.getTemplateName(),
                                HostArchitecture.AMD64, contSize );

                        containerGroupRequest.addRequest( cloneRequest );
                    }
                    catch ( Exception e )
                    {
                        LOG.error( "Could not create container clone request", e.getMessage() );
                    }
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
                        nodeDto.setHostName( cloneResponse.getHostname() );
                        nodeDto.setState( ContainerStateDto.RUNNING );

                        Set<Host> hosts = new HashSet<>();
                        Host host = peerManager.getLocalPeer().getContainerHostById( nodeDto.getContainerId() );
                        hosts.add( host );
                        String sshKey = createSshKey( hosts );
                        nodeDto.addSshKey( sshKey );
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


    public EnvironmentPeerDto configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto )
            throws EnvironmentManagerException
    {

        final EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        final LocalPeer localPeer = peerManager.getLocalPeer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final EnvironmentId environmentId = new EnvironmentId( env.getId() );
        final Set<String> sshKeys = new HashSet<>();
        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                if ( nodeDto.getSshKeys() != null )
                {
                    sshKeys.addAll( nodeDto.getSshKeys() );
                }
            }
        }

        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {
                localPeer.configureSshInEnvironment( environmentId, new SshPublicKeys( sshKeys ) );
                return localPeer;
            }
        } );

        try
        {
            Future<Peer> f = completionService.take();
            f.get();

            for ( SSHKeyDto sshKeyDto : env.getSshKeys() )
            {
                sshKeyDto.addConfiguredPeer( localPeer.getId() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to register ssh keys on peer: " + localPeer.getId(), e );
        }
        return peerDto;
    }


    public void configureHash( EnvironmentDto envDto ) throws EnvironmentManagerException
    {
        final LocalPeer localPeer = peerManager.getLocalPeer();

        final EnvironmentId environmentId = new EnvironmentId( envDto.getId() );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                hostAddresses.put( nodeDto.getHostName(), nodeDto.getIp() );
            }
        }
        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {
                localPeer.configureHostsInEnvironment( environmentId, new HostAddresses( hostAddresses ) );
                return localPeer;
            }
        } );

        try
        {
            Future<Peer> f = completionService.take();
            f.get();
        }
        catch ( Exception e )
        {
            LOG.error( "Problems registering hosts in peer: " + localPeer.getId(), e );
        }
    }


    public String createSshKey( Set<Host> hosts )
    {
        Map<Host, CommandResult> results = commandUtil.executeParallelSilent( getCreateNReadSSHCommand(), hosts );

        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            CommandResult result = resultEntry.getValue();
            if ( result.hasSucceeded() && !Strings.isNullOrEmpty( result.getStdOut() ) )
            {
                return result.getStdOut();
            }
            else
            {
                LOG.debug( String.format( "Error: %s, Exit Code %d", result.getStdErr(), result.getExitCode() ) );
            }
        }
        return null;
    }


    public RequestBuilder getCreateNReadSSHCommand()
    {
        return new RequestBuilder( String.format( "rm -rf %1$s && " +
                        "mkdir -p %1$s && " +
                        "chmod 700 %1$s && " +
                        "ssh-keygen -t dsa -P '' -f %1$s/id_dsa -q && " + "cat %1$s/id_dsa.pub",
                Common.CONTAINER_SSH_FOLDER ) );
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
            P2PConnections p2PConnections = peer.joinP2PSwarm( p2PConfig );
            for ( P2PConnection p2PConnection : p2PConnections.getConnections() )
            {
                p2PConfig.addP2pIp( p2PConnection.getIp() );
            }
            return p2PConfig;
        }
    }


    private class SetupTunnelTask implements Callable<Boolean>
    {
        private final Peer peer;
        private final String environmentId;
        private final P2pIps p2pIps;


        public SetupTunnelTask( final Peer peer, final String environmentId, P2pIps p2pIps )
        {
            this.peer = peer;
            this.environmentId = environmentId;
            this.p2pIps = p2pIps;
        }


        @Override
        public Boolean call() throws Exception
        {
            peer.setupTunnels( p2pIps, environmentId );
            return true;
        }
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    protected CompletionService<PrepareTemplatesResponseCollector> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
