package io.subutai.core.hubmanager.impl;


import java.util.Collection;
import java.util.HashMap;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
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
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.EnvironmentManager;
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
            LOG.error( "Could not setup VNI", e.getMessage() );
        }
    }


    public EnvironmentPeerDto setupP2P( EnvironmentPeerDto peerDto )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        SubnetUtils.SubnetInfo subnetInfo =
                new SubnetUtils( peerDto.getEnvironmentInfo().getTunnelNetwork(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        final String[] addresses = subnetInfo.getAllAddresses();

        try
        {
            localPeer.setupInitialP2PConnection(
                    new P2PConfig( localPeer.getId(), env.getId(), env.getP2pHash(), addresses[0], env.getP2pKey(),
                            env.getP2pTTL() ) );
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not setup initial p2p participant on local peer MH with explicit IP", e );
        }

        ExecutorService p2pExecutor = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        P2PConfig config =
                new P2PConfig( localPeer.getId(), env.getId(), env.getP2pHash(), addresses[1], env.getP2pKey(),
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
        Map<String, String> tunnels = new HashMap<>();

        for ( EnvironmentPeerDto peerDto : environmentDto.getPeers() )
        {
            if ( !peerDto.getPeerId().equals( localPeer.getId() ) )
            {
                tunnels.put( peerDto.getPeerId(), peerDto.getTunnelAddress() );
            }
        }

        if ( !tunnels.isEmpty() )
        {
            ExecutorService tunnelExecutor = Executors.newSingleThreadExecutor();
            ExecutorCompletionService<Integer> tunnelCompletionService =
                    new ExecutorCompletionService<Integer>( tunnelExecutor );

            tunnelCompletionService.submit( new SetupTunnelTask( localPeer, environmentDto.getId(), tunnels ) );

            final Future<Integer> f = tunnelCompletionService.take();
            f.get();

            tunnelExecutor.shutdown();
        }
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
                if ( nodeDto.getState().equals( ContainerStateDto.STARTING ) )
                {
                    ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
                    try
                    {
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


    public void configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto ) throws EnvironmentManagerException
    {
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        Set<String> sshKeys = new HashSet<>();
        for ( SSHKeyDto sshKeyDto : env.getSshKeys() )
        {
            sshKeys.add( sshKeyDto.getSshKey() );
        }
        Set<Host> hosts = getLocalPeerHosts( envDto );
        addSshKeys( hosts, sshKeys );

        Set<Host> succeededHosts = Sets.newHashSet();
        Set<Host> failedHosts = Sets.newHashSet( hosts );

        Map<Host, CommandResult> results = commandUtil.executeParallelSilent( getConfigSSHCommand(), hosts );

        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            CommandResult result = resultEntry.getValue();
            Host host = resultEntry.getKey();

            if ( result.hasSucceeded() )
            {
                succeededHosts.add( host );
            }
        }

        failedHosts.removeAll( succeededHosts );

        for ( Host failedHost : failedHosts )
        {
            LOG.error( String.format( "Failed to configure ssh on host %s", failedHost.getHostname() ) );
        }

        if ( !failedHosts.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to configure ssh on all hosts" );
        }
    }


    public void configureHash( EnvironmentDto envDto ) throws EnvironmentManagerException
    {
        Set<Host> hosts = getLocalPeerHosts( envDto );
        Map<Host, CommandResult> results = commandUtil
                .executeParallelSilent( getAddIpHostToEtcHostsCommand( Common.DEFAULT_DOMAIN_NAME, hosts ), hosts );

        Set<Host> succeededHosts = Sets.newHashSet();
        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            CommandResult result = resultEntry.getValue();
            Host host = resultEntry.getKey();

            if ( result.hasSucceeded() )
            {
                succeededHosts.add( host );
            }
            else
            {
                LOG.debug( String.format( "Error: %s, Exit Code %d", result.getStdErr(), result.getExitCode() ) );
            }
        }

        hosts.removeAll( succeededHosts );

        for ( Host failedHost : hosts )
        {
            LOG.error( String.format( "Host registration failed on host %s", failedHost.getHostname() ) );
        }

        if ( !hosts.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to register all hosts" );
        }
    }


    private Set<Host> getLocalPeerHosts( EnvironmentDto envDto )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        Set<Host> hosts = Sets.newHashSet();

        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            if ( nodesDto.getPeerId().equals( localPeer.getId() ) )
            {
                for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                {
                    try
                    {
                        hosts.add( localPeer.getContainerHostById( nodeDto.getHostName() ) );
                    }
                    catch ( HostNotFoundException e )
                    {
                        LOG.error( "Could not get Host: " + nodeDto.getHostId() );
                    }
                }
            }
        }
        return hosts;
    }


    protected void addSshKeys( Set<Host> hosts, Set<String> keys ) throws EnvironmentManagerException
    {
        //send key in portions, since all can not fit into one command, it fails
        int i = 0;
        StringBuilder keysString = new StringBuilder();
        for ( String key : keys )
        {
            keysString.append( key );
            i++;
            //send next 5 keys
            if ( i % 5 == 0 || i == keys.size() )
            {
                Set<Host> succeededHosts = Sets.newHashSet();
                Set<Host> failedHosts = Sets.newHashSet( hosts );

                Map<Host, CommandResult> results =
                        commandUtil.executeParallelSilent( getAppendSshKeysCommand( keysString.toString() ), hosts );

                keysString.setLength( 0 );

                for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
                {
                    CommandResult result = resultEntry.getValue();
                    Host host = resultEntry.getKey();

                    if ( result.hasSucceeded() )
                    {
                        succeededHosts.add( host );
                    }
                }

                failedHosts.removeAll( succeededHosts );

                for ( Host failedHost : failedHosts )
                {
                    LOG.error( String.format( "Failed to add ssh keys on host %s", failedHost.getHostname() ) );
                }

                if ( !failedHosts.isEmpty() )
                {
                    throw new EnvironmentManagerException( "Failed to add ssh keys on all hosts" );
                }
            }
        }
    }


    public RequestBuilder getAppendSshKeysCommand( String keys )
    {
        return new RequestBuilder( String.format( "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "echo '%3$s' >> %2$s && " +
                "chmod 644 %2$s", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, keys ) );
    }


    public RequestBuilder getConfigSSHCommand()
    {
        return new RequestBuilder( String.format( "echo 'Host *' > %1$s/config && " +
                "echo '    StrictHostKeyChecking no' >> %1$s/config && " +
                "chmod 644 %1$s/config", Common.CONTAINER_SSH_FOLDER ) );
    }


    public RequestBuilder getAddIpHostToEtcHostsCommand( String domainName, Set<Host> containerHosts )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( Host host : containerHosts )
        {
            String ip = host.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp();
            String hostname = host.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

        return new RequestBuilder( appendHosts.toString() );
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

    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    protected CompletionService<PrepareTemplatesResponseCollector> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
