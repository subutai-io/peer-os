package io.subutai.core.hubmanager.impl.environment;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Strings;

import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.environment.state.StateHandlerFactory;
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

    private final Context ctx;

    private final StateHandlerFactory handlerFactory;

    private final String linkPattern;

    // ===

    private final ConfigManager configManager;
    private final PeerManager peerManager;
    private final HubEnvironmentManager hubEnvManager;
    private final EnvironmentUserHelper envUserHelper;


    public HubEnvironmentProcessor( HubEnvironmentManager hubEnvManager, Context ctx )
    {
        this.hubEnvManager = hubEnvManager;

        this.ctx = ctx;

        this.configManager = null;
        this.peerManager = null;
        this.envUserHelper = null;

        handlerFactory = new StateHandlerFactory( ctx );

        linkPattern = "/rest/v1/environments/.*/peers/" + ctx.localPeer.getId();
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
        EnvironmentPeerDto peerDto = ctx.restClient.getStrict( link, EnvironmentPeerDto.class );

        StateHandler handler = handlerFactory.getHandler( peerDto.getState() );

        handler.handle( peerDto );
    }


    private void environmentBuildProcess( final EnvironmentPeerDto peerDto )
    {
        try
        {
            switch ( peerDto.getState() )
            {
                case CHANGE_CONTAINER_STATE:
                    controlContainer( peerDto );
                    break;
                case CONFIGURE_DOMAIN:
                    configureDomain( peerDto );
                    break;
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
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
}
