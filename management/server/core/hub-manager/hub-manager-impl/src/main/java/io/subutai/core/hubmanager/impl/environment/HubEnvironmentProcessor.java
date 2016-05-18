package io.subutai.core.hubmanager.impl.environment;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Strings;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.environment.state.StateHandlerFactory;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.hubmanager.impl.processor.EnvironmentUserHelper;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto.LogEvent;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto.LogType;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class HubEnvironmentProcessor implements StateLinkProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final ConfigManager configManager;

    private final PeerManager peerManager;

    private final HubEnvironmentManager hubEnvManager;

    private final EnvironmentUserHelper envUserHelper;

    private final Context ctx;

    private final StateHandlerFactory handlerFactory;

    private final String linkPattern;


    public HubEnvironmentProcessor( HubEnvironmentManager hubEnvManager, Context ctx )
    {
        this.hubEnvManager = hubEnvManager;

        this.ctx = ctx;

        this.configManager = ctx.configManager;

        this.peerManager = ctx.peerManager;

        this.envUserHelper = ctx.envUserHelper;

        handlerFactory = new StateHandlerFactory( ctx );

        linkPattern = "/rest/v1/environments/.*/peers/" + peerManager.getLocalPeer().getId();
    }


    @Override
    public void processStateLinks( Set<String> stateLinks ) throws HubPluginException
    {
        for ( String link : stateLinks )
        {
            if ( link.matches( linkPattern ) )
            {
                processStateLink( link );
            }
        }
    }


    private void processStateLink( String link ) throws HubPluginException
    {
        EnvironmentPeerDto peerDto = getEnvironmentPeerDto( link );

        StateHandler handler = handlerFactory.getHandler( peerDto.getState() );

        handler.handle( peerDto );
    }


    private EnvironmentPeerDto getEnvironmentPeerDto( String link ) throws HubPluginException
    {
        RestResult<EnvironmentPeerDto> restResult = ctx.restClient.get( link, EnvironmentPeerDto.class );

        if ( !restResult.isSuccess() )
        {
            throw new HubPluginException( restResult.getError() );
        }

        return restResult.getEntity();
    }


    private void environmentBuildProcess( final EnvironmentPeerDto peerDto )
    {
        try
        {
            switch ( peerDto.getState() )
            {
//                case EXCHANGE_INFO:
//                    infoExchange( peerDto );
//                    break;
                case RESERVE_NETWORK:
                    reserveNetwork( peerDto );
                    break;
                case SETUP_TUNNEL:
                    setupTunnel( peerDto );
                    break;
                case BUILD_CONTAINER:
                    buildContainers( peerDto );
                    break;
                case CONFIGURE_CONTAINER:
                    configureContainer( peerDto );
                    break;
                case CHANGE_CONTAINER_STATE:
                    controlContainer( peerDto );
                    break;
                case CONFIGURE_DOMAIN:
                    configureDomain( peerDto );
                    break;
                case DELETE_PEER:
                    deletePeer( peerDto );
                    break;
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void reserveNetwork( EnvironmentPeerDto peerDto )
    {
        try
        {
            hubEnvManager.reserveNetworkResource( peerDto );
            peerDto = hubEnvManager.setupP2P( peerDto );
            updateEnvironmentPeerData( peerDto );
        }
        catch ( EnvironmentCreationException e )
        {
            log.error( e.getMessage() );
        }
    }


    private void setupTunnel( EnvironmentPeerDto peerDto )
    {
        String setupTunnelDataURL = String.format( "/rest/v1/environments/%s", peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client =
                    configManager.getTrustedWebClientWithAuth( setupTunnelDataURL, configManager.getHubIp() );
            Response r = client.get();
            client.close();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentDto environmentDto = JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
            peerDto.setSetupTunnel( false );
            hubEnvManager.setupTunnel( peerDto, environmentDto );
            updateEnvironmentPeerData( peerDto );
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            String mgs = "Could not get environment data from Hub.";
            hubEnvManager
                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );
            log.error( mgs, e );
        }
    }


    private void buildContainers( EnvironmentPeerDto peerDto )
    {
        String containerDataURL = String.format( "/rest/v1/environments/%s/container-build-workflow",
                peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( containerDataURL, configManager.getHubIp() );
            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentNodesDto envNodes = JsonUtil.fromCbor( plainContent, EnvironmentNodesDto.class );

            hubEnvManager.prepareTemplates( peerDto, envNodes, peerDto.getEnvironmentInfo().getId() );

            EnvironmentNodesDto updatedNodes = hubEnvManager.cloneContainers( peerDto, envNodes );

            byte[] cborData = JsonUtil.toCbor( updatedNodes );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response response = client.put( encryptedData );
            client.close();

            log.debug( "response.status: {}", response.getStatus() );

            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                log.debug( "env_via_hub: Environment successfully build!!!" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not get container creation data from Hub.";

            hubEnvManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );

            log.error( mgs, e );
        }
    }


    private void configureContainer( EnvironmentPeerDto peerDto )
    {
        String configContainer = String.format( "/rest/v1/environments/%s/container-configuration",
                peerDto.getEnvironmentInfo().getId() );
        try
        {
            EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );

            peerDto = hubEnvManager.configureSsh( peerDto, environmentDto );
            hubEnvManager.configureHash( peerDto, environmentDto );

            WebClient clientUpdate =
                    configManager.getTrustedWebClientWithAuth( configContainer, configManager.getHubIp() );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            Response response = clientUpdate.put( encryptedData );
            clientUpdate.close();
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                log.debug( "SSH configuration successfully done" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not configure SSH/Hash";
            log.error( mgs, e );
        }
    }


    private void controlContainer( EnvironmentPeerDto peerDto )
    {
        String controlContainerPath =
                String.format( "/rest/v1/environments/%s/peers/%s/container", peerDto.getEnvironmentInfo().getId(),
                        peerDto.getPeerId() );
        LocalPeer localPeer = peerManager.getLocalPeer();

        EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );
        if ( environmentDto != null )
        {
            for ( EnvironmentNodesDto nodesDto : environmentDto.getNodes() )
            {
                PeerId peerId = new PeerId( nodesDto.getPeerId() );
                EnvironmentId envId = new EnvironmentId( nodesDto.getEnvironmentId() );
                if ( nodesDto.getPeerId().equals( localPeer.getId() ) )
                {
                    for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                    {
                        ContainerId containerId =
                                new ContainerId( nodeDto.getContainerId(), nodeDto.getHostName(), peerId, envId );
                        try
                        {
                            if ( nodeDto.getState().equals( ContainerStateDto.STOPPING ) )
                            {
                                localPeer.stopContainer( containerId );
                                nodeDto.setState( ContainerStateDto.STOPPED );
                            }
                            if ( nodeDto.getState().equals( ContainerStateDto.STARTING ) )
                            {
                                localPeer.startContainer( containerId );
                                nodeDto.setState( ContainerStateDto.RUNNING );
                            }
                            if ( nodeDto.getState().equals( ContainerStateDto.ABORTING ) )
                            {
                                localPeer.destroyContainer( containerId );
                                nodeDto.setState( ContainerStateDto.FROZEN );
                            }
                        }
                        catch ( PeerException e )
                        {
                            String mgs = "Could not change container state";
                            hubEnvManager
                                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.CONTAINER, LogType.ERROR,
                                            containerId.getId() );
                            log.error( mgs, e );
                        }
                    }

                    try
                    {
                        WebClient clientUpdate = configManager
                                .getTrustedWebClientWithAuth( controlContainerPath, configManager.getHubIp() );

                        byte[] cborData = JsonUtil.toCbor( nodesDto );
                        byte[] encryptedData = configManager.getMessenger().produce( cborData );

                        Response response = clientUpdate.put( encryptedData );
                        clientUpdate.close();
                        if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
                        {
                            log.debug( "Container successfully updated" );
                        }
                    }
                    catch ( Exception e )
                    {
                        String mgs = "Could not send containers state to hub";
                        hubEnvManager
                                .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR,
                                        null );
                        log.error( mgs, e );
                    }
                }
            }
        }
    }


    private void configureDomain( EnvironmentPeerDto peerDto )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        String domainUpdatePath =
                String.format( "/rest/v1/environments/%s/peers/%s/domain", peerDto.getEnvironmentInfo().getId(),
                        peerDto.getPeerId() );
        try
        {
            //TODO balanceStrategy should come from HUB
            EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );
            boolean assign = !Strings.isNullOrEmpty( env.getDomainName() );
            assert environmentDto != null;
            if ( assign )
            {
                ProxyLoadBalanceStrategy balanceStrategy = ProxyLoadBalanceStrategy.LOAD_BALANCE;
                localPeer.setVniDomain( env.getVni(), env.getDomainName(), balanceStrategy, env.getSslCertPath() );
                for ( EnvironmentNodesDto nodesDto : environmentDto.getNodes() )
                {
                    if ( nodesDto.getPeerId().equals( localPeer.getId() ) )
                    {
                        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                        {
                            try
                            {
                                localPeer.addIpToVniDomain( nodeDto.getIp(), env.getVni() );
                            }
                            catch ( Exception e )
                            {
                                log.error( "Could not add container IP to domain: " + nodeDto.getContainerName() );
                            }
                        }
                    }
                }
            }
            else
            {
                localPeer.removeVniDomain( env.getVni() );
            }

            WebClient clientUpdate =
                    configManager.getTrustedWebClientWithAuth( domainUpdatePath, configManager.getHubIp() );
            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response response = clientUpdate.put( encryptedData );
            clientUpdate.close();
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                log.debug( "Domain configuration successfully done" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not configure domain name";
            hubEnvManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.SUBUTAI, LogType.ERROR, null );
            log.error( mgs, e );
        }
    }


    private void deletePeer( EnvironmentPeerDto peerDto )
    {
        String urlFormat = "/rest/v1/environments/%s/peers/%s";

        String containerDestroyStateURL =
                String.format( urlFormat, peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId() );

        LocalPeer localPeer = peerManager.getLocalPeer();

        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        try
        {
            EnvironmentId envId = new EnvironmentId( env.getId() );

            localPeer.cleanupEnvironment( envId );
            ReservedNetworkResources reservedNetworkResources = localPeer.getReservedNetworkResources();
            for ( NetworkResource networkResource : reservedNetworkResources.getNetworkResources() )
            {
                if ( networkResource.getEnvironmentId().equals( env.getId() ) )
                {
                    throw new Exception( "Environment network resources are not cleaned yet." );
                }
            }

            envUserHelper.handleEnvironmentOwnerDeletion( peerDto );

            WebClient client =
                    configManager.getTrustedWebClientWithAuth( containerDestroyStateURL, configManager.getHubIp() );

            Response response = client.delete();
            client.close();
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                log.debug( "Environment data cleaned successfully" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not clean environment";
            hubEnvManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.SUBUTAI, LogType.ERROR, null );
            log.error( mgs, e );
        }
    }


    private EnvironmentDto getEnvironmentDto( String envId )
    {
        String envDataPath = String.format( "/rest/v1/environments/%s", envId );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( envDataPath, configManager.getHubIp() );
            Response r = client.get();
            client.close();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            return JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
        }
        catch ( Exception e )
        {
            log.error( "Could not get environment data from Hub", e );
        }
        return null;
    }


    private void updateEnvironmentPeerData( EnvironmentPeerDto peerDto )
    {
        try
        {
            String envPeerDataUrl =
                    String.format( "/rest/v1/environments/%s/peers/%s", peerDto.getEnvironmentInfo().getId(),
                            peerManager.getLocalPeer().getId() );
            WebClient client = configManager.getTrustedWebClientWithAuth( envPeerDataUrl, configManager.getHubIp() );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.put( encryptedData );
            client.close();
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                String mgs = "Environment peer data successfully sent to hub";
                hubEnvManager.sendLogToHub( peerDto, mgs, null, LogEvent.REQUEST_TO_HUB, LogType.DEBUG, null );
                log.debug( mgs );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent environment peer data to hub.";
            hubEnvManager
                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );
            log.error( mgs, e.getMessage() );
        }
    }
}
