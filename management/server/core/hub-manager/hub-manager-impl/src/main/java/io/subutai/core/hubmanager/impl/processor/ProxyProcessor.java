package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Protocol;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.domain.P2PInfoDto;
import io.subutai.hub.share.dto.domain.PortMapDto;
import io.subutai.hub.share.dto.domain.ProxyDto;


public class ProxyProcessor implements StateLinkProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final HashSet<String> LINKS_IN_PROGRESS = new HashSet<>();

    private ConfigManager configManager;

    private PeerManager peerManager;

    private HubRestClient restClient;


    public ProxyProcessor()
    {
    }


    public ProxyProcessor( ConfigManager configManager, PeerManager peerManager, HubRestClient restClient )
    {
        this.configManager = configManager;
        this.peerManager = peerManager;
        this.restClient = restClient;
    }


    @Override
    public synchronized boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "proxies" ) )
            {
                processStateLink( stateLink );
            }
        }

        return false;
    }


    private void processStateLink( String stateLink )
    {
        try
        {
            if ( LINKS_IN_PROGRESS.contains( stateLink ) )
            {
                log.info( "This link is in progress: {}", stateLink );

                return;
            }

            LINKS_IN_PROGRESS.add( stateLink );

            RestResult<ProxyDto> result = restClient.get( stateLink, ProxyDto.class );

            ProxyDto proxyDto = result.getEntity();

            for ( P2PInfoDto p2PInfoDto : proxyDto.getP2PInfoDtos() )
            {
                switch ( p2PInfoDto.getState() )
                {
                    case CREATE:
                        setupP2PTunnel( proxyDto, p2PInfoDto );
                        setupPortMap( proxyDto, stateLink );
                        break;
                    case UPDATE:
                        setupPortMap( proxyDto, stateLink );
                        break;
                    case DESTROY:
                        try
                        {
                            destroyTunnel( proxyDto, stateLink );
                        }
                        catch ( Exception e )
                        {
                            log.info( e.getMessage() );
                        }
                        break;
                    default:
                        wrongState( proxyDto );
                        break;
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        finally
        {
            LINKS_IN_PROGRESS.remove( stateLink );
        }
    }


    private void wrongState( ProxyDto proxyDto )
    {
        log.error( "Wrong state come from HUB: {}", proxyDto );
    }


    private void destroyTunnel( ProxyDto proxyDto, String stateLink ) throws Exception
    {
        for ( P2PInfoDto p2PInfoDto : proxyDto.getP2PInfoDtos() )
        {
            if ( p2PInfoDto.getRhId() != null && p2PInfoDto.getState().equals( P2PInfoDto.State.DESTROY ) )
            {

                ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostById( p2PInfoDto.getRhId() );

                cleanPortMap( resourceHost, proxyDto );

                resourceHost.removeP2PSwarm( proxyDto.getP2pHash() );
            }
        }

        sendDataToHub( proxyDto, stateLink );
    }


    private void cleanPortMap( ResourceHost resourceHost, ProxyDto proxyDto )
    {
        try
        {
            for ( PortMapDto portMapDto : proxyDto.getPortMaps() )
            {
                if ( resourceHost.isPortMappingReserved( Protocol.valueOf( portMapDto.getProtocol().name() ),
                        portMapDto.getExternalPort(), portMapDto.getProxyIp(), portMapDto.getExternalPort() ) )
                {
                    resourceHost.removeContainerPortMapping( Protocol.valueOf( portMapDto.getProtocol().name() ),
                            portMapDto.getProxyIp(), portMapDto.getExternalPort(), portMapDto.getExternalPort() );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void setupPortMap( ProxyDto proxyDto, String stateLink )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            for ( PortMapDto portMapDto : proxyDto.getPortMaps() )
            {

                if ( resourceHost.isPortMappingReserved( Protocol.valueOf( portMapDto.getProtocol().name() ),
                        portMapDto.getExternalPort(), portMapDto.getProxyIp(), portMapDto.getExternalPort() ) )
                {

                    if ( portMapDto.getState().equals( PortMapDto.State.DESTROYING ) || portMapDto.getState().equals(
                            PortMapDto.State.DELETED ) )
                    {
                        resourceHost.removeContainerPortMapping( Protocol.valueOf( portMapDto.getProtocol().name() ),
                                portMapDto.getProxyIp(), portMapDto.getExternalPort(), portMapDto.getExternalPort() );
                    }
                }
                else if ( portMapDto.getState().equals( PortMapDto.State.CREATING ) || portMapDto.getState().equals(
                        PortMapDto.State.USED ) )
                {
                    resourceHost.mapContainerPort( Protocol.valueOf( portMapDto.getProtocol().name() ),
                            portMapDto.getProxyIp(), portMapDto.getExternalPort(), portMapDto.getExternalPort() );
                }
            }

            sendDataToHub( proxyDto, stateLink );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void setupP2PTunnel( ProxyDto proxyDto, P2PInfoDto p2PInfoDto )
    {
        try
        {
            if ( p2PInfoDto.getRhId() != null && p2PInfoDto.getState().equals( P2PInfoDto.State.CREATE ) )
            {

                ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostById( p2PInfoDto.getRhId() );

                resourceHost.joinP2PSwarm( p2PInfoDto.getP2pIp(), p2PInfoDto.getIntefaceName(), proxyDto.getP2pHash(),
                        proxyDto.getP2SecretKey(), proxyDto.getP2pSecretTTL().longValue() );

                p2PInfoDto.setState( P2PInfoDto.State.READY );
            }
        }
        catch ( Exception e )
        {
            p2PInfoDto.setState( P2PInfoDto.State.FAILED );
            p2PInfoDto.setLogs( e.getMessage() );
            log.error( e.getMessage() );
        }
    }


    private void collectP2PSubnets( ProxyDto proxyDto, String stateLink )
    {
        try
        {
            Set<String> subnets = new HashSet<>();

            for ( NetworkResource networkResource : peerManager.getLocalPeer().getReservedNetworkResources()
                                                               .getNetworkResources() )
            {
                subnets.add( networkResource.getP2pSubnet() );
            }

            proxyDto.setSubnets( subnets );

            sendDataToHub( proxyDto, stateLink );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void sendDataToHub( ProxyDto proxyDto, String link )
    {
        RestResult<Object> result = restClient.post( link, proxyDto );

        if ( !result.isSuccess() )
        {
            log.error( "Error to send  data to Hub: " + result.getError() );
        }
        else
        {
            log.info( "Sent Data to HUB Success" );
        }
    }
}
