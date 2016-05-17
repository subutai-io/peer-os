package io.subutai.core.hubmanager.impl;


import java.util.HashSet;
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

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.RhP2pIp;
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
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.hubmanager.impl.entity.RhP2PIpEntity;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.PublicKeyContainer;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerRHDto;
import io.subutai.hub.share.dto.environment.SSHKeyDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class HubEnvironmentManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private CommandUtil commandUtil = new CommandUtil();

    private PeerManager peerManager;

    private ConfigManager configManager;


    public HubEnvironmentManager( ConfigManager configManager, PeerManager peerManager )
    {
        this.configManager = configManager;
        this.peerManager = peerManager;
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
            if ( e.getMessage().contains( "Error reserving network resources" ) && e.getMessage().contains( "already reserved" ) )
            {
                return;
            }

            String msg = "Failed to reserve network resources on Peer ID: " + localPeer.getId();

            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.NETWORK,
                    EnvironmentPeerLogDto.LogType.ERROR, null );

            log.error( msg, e.getMessage() );

            throw new EnvironmentCreationException( msg );
        }
    }


    public PublicKeyContainer createPeerEnvironmentKeyPair( RelationLinkDto envLink ) throws PeerException
    {
        io.subutai.common.security.PublicKeyContainer publicKeyContainer =
                peerManager.getLocalPeer().createPeerEnvironmentKeyPair( envLink );

        PublicKeyContainer keyContainer = new PublicKeyContainer();
        keyContainer.setKey( publicKeyContainer.getKey() );
        keyContainer.setHostId( publicKeyContainer.getHostId() );
        keyContainer.setFingerprint( publicKeyContainer.getFingerprint() );

        return keyContainer;
    }


    public EnvironmentPeerDto setupP2P( EnvironmentPeerDto peerDto ) throws EnvironmentCreationException
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        ExecutorService p2pExecutor = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        P2PConfig config =
                new P2PConfig( localPeer.getId(), env.getId(), env.getP2pHash(), env.getP2pKey(), env.getP2pTTL() );

        for ( EnvironmentPeerRHDto rhDto : peerDto.getRhs() )
        {
            config.addRhP2pIp( new RhP2PIpEntity( rhDto.getId(), rhDto.getP2pIp() ) );
        }
        p2pCompletionService.submit( new SetupP2PConnectionTask( localPeer, config ) );

        try
        {

            final Future<P2PConfig> f = p2pCompletionService.take();
            f.get();
            p2pExecutor.shutdown();
        }
        catch ( ExecutionException | InterruptedException e )
        {
            String msg = "Failed to setup P2P connection on Peer ID: " + localPeer.getId();
            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.NETWORK,
                    EnvironmentPeerLogDto.LogType.ERROR, null );
            log.error( msg, e.getMessage() );

            throw new EnvironmentCreationException( msg );
        }
        return peerDto;
    }


    public EnvironmentPeerDto setupTunnel( EnvironmentPeerDto peerDto, EnvironmentDto environmentDto )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        Set<RhP2pIp> setOfP2PIps = new HashSet<>();
        P2pIps p2pIps = new P2pIps();

        for ( EnvironmentPeerDto peerDt : environmentDto.getPeers() )
        {
            for ( EnvironmentPeerRHDto rhDto : peerDt.getRhs() )
            {
                setOfP2PIps.add( new RhP2PIpEntity( rhDto.getId(), rhDto.getP2pIp() ) );
            }
        }
        p2pIps.addP2pIps( setOfP2PIps );
        if ( !p2pIps.isEmpty() )
        {
            ExecutorService tunnelExecutor = Executors.newSingleThreadExecutor();
            ExecutorCompletionService<Boolean> tunnelCompletionService =
                    new ExecutorCompletionService<>( tunnelExecutor );

            tunnelCompletionService.submit( new SetupTunnelTask( localPeer, environmentDto.getId(), p2pIps ) );

            try
            {
                final Future<Boolean> f = tunnelCompletionService.take();
                f.get();
                peerDto.setSetupTunnel( true );
                tunnelExecutor.shutdown();
            }
            catch ( ExecutionException | InterruptedException e )
            {
                String msg = "Failed to setup tunnel on Peer ID: " + localPeer.getId();
                sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.NETWORK,
                        EnvironmentPeerLogDto.LogType.ERROR, null );
                log.error( msg, e.getMessage() );
            }
        }
        return peerDto;
    }


    public void prepareTemplates( EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto, String environmentId )
            throws EnvironmentCreationException
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
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
        CompletionService<PrepareTemplatesResponse> taskCompletionService = getCompletionService( taskExecutor );

        taskCompletionService.submit( new CreatePeerTemplatePrepareTask( environmentId, localPeer, nodes ) );
        taskExecutor.shutdown();

        try
        {
            Future<PrepareTemplatesResponse> futures = taskCompletionService.take();
            final PrepareTemplatesResponse prepareTemplatesResponse = futures.get();

            if ( !prepareTemplatesResponse.hasSucceeded() )
            {
                for ( String templateResponse : prepareTemplatesResponse.getMessages() )
                {
                    String msg =
                            "Error during preparation template: " + templateResponse + " Peer ID: " + localPeer.getId();
                    sendLogToHub( peerDto, msg, null, EnvironmentPeerLogDto.LogEvent.SUBUTAI,
                            EnvironmentPeerLogDto.LogType.ERROR, null );
                    log.error( msg );
                    throw new EnvironmentCreationException( msg );
                }
            }
        }
        catch ( Exception e )
        {
            String msg = "There were errors during preparation templates. Unexpected error.";
            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
                    EnvironmentPeerLogDto.LogType.ERROR, null );
            log.error( msg, e.getMessage() );
            throw new EnvironmentCreationException( msg );
        }
    }


    public EnvironmentNodesDto cloneContainers( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes )
            throws EnvironmentCreationException
    {
        CreateEnvironmentContainersRequest containerGroupRequest =
                new CreateEnvironmentContainersRequest( peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId(),
                        peerDto.getOwnerId() );

        Set<EnvironmentNodeDto> failedNodes = new HashSet<>();

        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
        {
            if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
            {
                failedNodes.add( nodeDto );
                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );

                nodeDto.setState( ContainerStateDto.UNKNOWN );
                CloneRequest cloneRequest =
                        new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                                nodeDto.getIp(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );

                containerGroupRequest.addRequest( cloneRequest );
            }
        }

        final CreateEnvironmentContainersResponse containerCollector;

        try
        {
            containerCollector = peerManager.getLocalPeer().createEnvironmentContainers( containerGroupRequest );

            Set<CloneResponse> cloneResponseList = containerCollector.getResponses();

            for ( CloneResponse cloneResponse : cloneResponseList )
            {
                for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
                {
                    if ( cloneResponse.getHostname().equals( nodeDto.getHostName() ) )
                    {
                        failedNodes.remove( nodeDto );

                        nodeDto.setIp( cloneResponse.getIp() );
                        nodeDto.setTemplateArch( cloneResponse.getTemplateArch().name() );
                        nodeDto.setContainerId( cloneResponse.getContainerId() );
                        nodeDto.setElapsedTime( cloneResponse.getElapsedTime() );
                        nodeDto.setHostName( cloneResponse.getHostname() );
                        nodeDto.setState( ContainerStateDto.RUNNING );

                        Set<Host> hosts = new HashSet<>();
                        Host host = peerManager.getLocalPeer().getContainerHostById( nodeDto.getContainerId() );
                        hosts.add( host );

                        String sshKey = createSshKey( hosts, peerDto.getEnvironmentInfo().getId() );
                        nodeDto.addSshKey( sshKey );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            String msg = "Failed on cloning container: ";

            for ( EnvironmentNodeDto nodeDto : failedNodes )
            {
                msg += nodeDto.getContainerId();
                sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.CONTAINER,
                        EnvironmentPeerLogDto.LogType.ERROR, nodeDto.getContainerId() );
                log.error( msg, e.getMessage() );
            }
            throw new EnvironmentCreationException( msg );
        }

        if ( failedNodes.size() != 0 )
        {
            String msg = "Failed on cloning container: ";

            for ( EnvironmentNodeDto nodeDto : failedNodes )
            {
                sendLogToHub( peerDto, msg + nodeDto.getContainerId(), null, EnvironmentPeerLogDto.LogEvent.CONTAINER,
                        EnvironmentPeerLogDto.LogType.ERROR, nodeDto.getContainerId() );

                log.error( msg + nodeDto.getContainerId() );
            }

            throw new EnvironmentCreationException( msg );
        }

        return envNodes;
    }


    public EnvironmentPeerDto configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto )
            throws EnvironmentManagerException, EnvironmentCreationException
    {

        final EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        final LocalPeer localPeer = peerManager.getLocalPeer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final EnvironmentId environmentId = new EnvironmentId( env.getId() );
        final SshKeys sshKeys = new SshKeys();
        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                if ( nodeDto.getSshKeys() != null )
                {
                    sshKeys.addStringKeys( nodeDto.getSshKeys() );
                }
            }
        }

        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {

                localPeer.configureSshInEnvironment( environmentId, sshKeys );
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
            String msg = "Failed to register ssh keys on peer: " + localPeer.getId();
            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
                    EnvironmentPeerLogDto.LogType.ERROR, null );
            log.error( msg, e );
            throw new EnvironmentCreationException( msg );
        }

        return peerDto;
    }


    public void configureHash( EnvironmentPeerDto peerDto, EnvironmentDto envDto )
            throws EnvironmentManagerException, EnvironmentCreationException
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
            String msg = "Problems registering hosts in peer: " + localPeer.getId();
            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
                    EnvironmentPeerLogDto.LogType.ERROR, null );
            log.error( msg, e );
            throw new EnvironmentCreationException( msg );
        }
    }


    public String createSshKey( Set<Host> hosts, String environmentId )
    {

        CommandUtil.HostCommandResults results =
                commandUtil.execute( getCreateNReadSSHCommand(), hosts, environmentId );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( result.hasSucceeded() && !Strings.isNullOrEmpty( result.getCommandResult().getStdOut() ) )
            {
                return result.getCommandResult().getStdOut();
            }
            else
            {
                log.debug( String.format( "Error: %s, Exit Code %d", result.getCommandResult().getStdErr(),
                        result.getCommandResult().getExitCode() ) );
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
            peer.joinP2PSwarm( p2PConfig );

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
            peer.setupTunnels( p2pIps, new EnvironmentId( environmentId ) );
            return true;
        }
    }


    protected CompletionService<PrepareTemplatesResponse> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }


    public void sendLogToHub( EnvironmentPeerDto peerDto, String msg, String exMsg, EnvironmentPeerLogDto.LogEvent logE,
                              EnvironmentPeerLogDto.LogType logType, String contId )
    {
        try
        {
            String envPeerLogPath =
                    String.format( "/rest/v1/environments/%s/peers/%s/log", peerDto.getEnvironmentInfo().getId(),
                            peerManager.getLocalPeer().getId() );
            WebClient client = configManager.getTrustedWebClientWithAuth( envPeerLogPath, configManager.getHubIp() );

            EnvironmentPeerLogDto peerLogDto = new EnvironmentPeerLogDto( peerDto.getPeerId(), peerDto.getState(),
                    peerDto.getEnvironmentInfo().getId(), logType );
            peerLogDto.setMessage( msg );
            peerLogDto.setExceptionMessage( exMsg );
            peerLogDto.setLogEvent( logE );
            peerLogDto.setContainerId( contId );
            peerLogDto.setLogCode( null );

            byte[] cborData = JsonUtil.toCbor( peerLogDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.post( encryptedData );
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                log.debug( "Environment peer log successfully sent to hub" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Could not sent environment peer log to hub.", e.getMessage() );
        }
    }
}
