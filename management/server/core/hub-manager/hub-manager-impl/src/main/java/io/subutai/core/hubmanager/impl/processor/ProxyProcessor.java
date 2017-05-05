package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.common.protocol.ReservedPorts;
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
            if ( stateLink.contains( "subnets" ) )
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

            switch ( proxyDto.getState() )
            {
                case COLLECT_P2P_SUBNETS:
                    collectP2PSubnets( proxyDto, stateLink );
                    break;
                case SETUP_TUNNEL:
                    setupP2PTunnel( proxyDto, stateLink );
                    setupPortMap( proxyDto, stateLink );
                    break;
                case SETUP_PORT_MAP:
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
                case READY:
                    wrongState( proxyDto );
                    break;
                case FAIED:
                    wrongState( proxyDto );
                    break;
                case WAIT:
                    wrongState( proxyDto );
                    break;
                default:
                    wrongState( proxyDto );
                    break;
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

                cleanPortMap( resourceHost, p2PInfoDto );

                resourceHost.removeP2PSwarm( proxyDto.getP2pHash() );
            }
        }

        sendDataToHub( proxyDto, stateLink );
    }


    private void cleanPortMap( ResourceHost resourceHost, P2PInfoDto p2PInfoDto )
    {
        ReservedPorts reservedPorts = null;
        try
        {
            reservedPorts = resourceHost.getContainerPortMappings( Protocol.TCP );

            for ( ReservedPort reservedPort : reservedPorts.getReservedPorts() )
            {
                if ( reservedPort.getContainerIpPort() != null && !reservedPort.getContainerIpPort().isEmpty() )
                {
                    if ( reservedPort.getContainerIpPort().contains( p2PInfoDto.getP2pIp() ) )
                    {
                        //Protocol protocol, String containerIp, int containerPort, int rhPort
                        resourceHost
                                .removeContainerPortMapping( Protocol.TCP, getIp( reservedPort.getContainerIpPort() ),
                                        getPort( reservedPort.getContainerIpPort() ), reservedPort.getPort() );
                    }
                }
            }
        }
        catch ( ResourceHostException e )
        {
            log.error( e.getMessage() );
        }
    }


    private String getIp( final String ipPort )
    {
        return ipPort.split( ":" )[0];
    }


    private int getPort( final String ipPort )
    {
        return Integer.valueOf( ipPort.split( ":" )[1] );
    }


    private void setupPortMap( ProxyDto proxyDto, String stateLink )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            for ( PortMapDto portMapDto : proxyDto.getPortMaps() )
            {
                resourceHost
                        .mapContainerPort( Protocol.valueOf( portMapDto.getProtocol().name() ), portMapDto.getProxyIp(),
                                portMapDto.getExternalPort(), portMapDto.getExternalPort() );
            }

            sendDataToHub( proxyDto, stateLink );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void setupP2PTunnel( ProxyDto proxyDto, String stateLink )
    {
        try
        {
            for ( P2PInfoDto p2PInfoDto : proxyDto.getP2PInfoDtos() )
            {
                if ( p2PInfoDto.getRhId() != null && p2PInfoDto.getState().equals( P2PInfoDto.State.SETUP_TUNNEL ) )
                {

                    ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostById( p2PInfoDto.getRhId() );

                    resourceHost
                            .joinP2PSwarm( p2PInfoDto.getP2pIp(), p2PInfoDto.getIntefaceName(), proxyDto.getP2pHash(),
                                    proxyDto.getP2SecretKey(), proxyDto.getP2pSecretTTL().longValue() );

                    p2PInfoDto.setState( P2PInfoDto.State.READY );
                }
            }

            //            sendDataToHub( proxyDto, stateLink );
        }
        catch ( Exception e )
        {
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
