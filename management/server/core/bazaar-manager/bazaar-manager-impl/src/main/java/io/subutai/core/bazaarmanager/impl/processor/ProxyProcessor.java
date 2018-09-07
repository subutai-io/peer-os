package io.subutai.core.bazaarmanager.impl.processor;


import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.Protocol;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.domain.PortMapDto;
import io.subutai.bazaar.share.dto.domain.ProxyDto;
import io.subutai.bazaar.share.dto.domain.ReservedPortMapping;


public class ProxyProcessor implements StateLinkProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final Set<String> LINKS_IN_PROGRESS = Sets.newConcurrentHashSet();

    private PeerManager peerManager;

    private RestClient restClient;


    public ProxyProcessor( PeerManager peerManager, RestClient restClient )
    {
        this.peerManager = peerManager;
        this.restClient = restClient;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws BazaarManagerException
    {
        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "/proxies/" ) )
            {
                processStateLink( stateLink );
            }
        }

        return false;
    }


    private void processStateLink( String stateLink )
    {
        if ( LINKS_IN_PROGRESS.contains( stateLink ) )
        {
            log.info( "This link is in progress: {}", stateLink );

            return;
        }

        LINKS_IN_PROGRESS.add( stateLink );

        try
        {
            RestResult<ProxyDto> result = restClient.get( stateLink, ProxyDto.class );

            ProxyDto proxyDto = result.getEntity();

            switch ( proxyDto.getState() )
            {
                case CREATE:
                    // join env. p2p swarm, get IP address of newly created p2p interface
                    joinEnvironmentP2PSwarm( proxyDto );
                    // create port maps
                    setupPortMap( proxyDto );
                    break;
                case UPDATE:
                    // add new or delete old port maps
                    setupPortMap( proxyDto );
                    proxyDto.setState( ProxyDto.State.READY );
                    break;
                case DESTROY:
                    // delete all port maps by p2p ip
                    cleanPortMappingsByProxy( proxyDto );
                    // delete p2p swarm
                    destroyTunnel( proxyDto );
                    // delete p2p network interface if it's not deleted
                    deleteP2pNetworkInterface( proxyDto );
                    break;
                default:
                    log.error( "Wrong state from Bazaar: {}", proxyDto );
                    break;
            }

            sendDataToBazaar( proxyDto, stateLink );
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


    private void destroyTunnel( ProxyDto proxyDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            resourceHost.removeP2PSwarm( proxyDto.getP2pHash() );
        }
        catch ( Exception e )
        {
            log.error( "Failed to remove proxy p2p swarm", e.getMessage() );
        }
    }


    private void deleteP2pNetworkInterface( ProxyDto proxyDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            if ( StringUtils.isNotBlank( proxyDto.getP2pIfaceName() ) )
            {
                resourceHost.removeP2PNetworkIface( proxyDto.getP2pIfaceName() );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failed to delete proxy p2p network interface: {}", e.getMessage() );
        }
    }


    private void cleanPortMappingsByProxy( ProxyDto proxyDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            Protocol protocol;

            for ( final PortMapDto portMapDto : proxyDto.getPortMaps() )
            {
                protocol = Protocol.valueOf( portMapDto.getProtocol().name() );

                if ( portMapDto.getProtocol().isHttpOrHttps() )
                {
                    resourceHost.removeContainerPortDomainMapping( protocol, portMapDto.getIpAddr(),
                            portMapDto.getExternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain() );

                    portMapDto.setState( PortMapDto.State.DELETED );
                }
                else
                {
                    resourceHost
                            .removeContainerPortMapping( protocol, portMapDto.getIpAddr(), portMapDto.getExternalPort(),
                                    portMapDto.getExternalPort() );

                    portMapDto.setState( PortMapDto.State.DELETED );
                }
            }

            // cleanup port mappings, if any is left. Search them by proxy p2p IP interface.
            String proxyP2PIpAddr = proxyDto.getP2pIpAddr();
            proxyP2PIpAddr = proxyP2PIpAddr.substring( 0, 1 + proxyP2PIpAddr.lastIndexOf( '.' ) );

            List<ReservedPortMapping> reservedPortMappings = resourceHost.getReservedPortMappings();

            for ( final ReservedPortMapping pm : reservedPortMappings )
            {
                if ( pm.getIpAddress().startsWith( proxyP2PIpAddr ) )
                {
                    protocol = Protocol.valueOf( pm.getProtocol().name() );

                    if ( pm.getProtocol().isHttpOrHttps() )
                    {
                        resourceHost
                                .removeContainerPortDomainMapping( protocol, pm.getIpAddress(), pm.getExternalPort(),
                                        pm.getExternalPort(), pm.getDomain() );
                    }
                    else
                    {
                        resourceHost.removeContainerPortMapping( protocol, pm.getIpAddress(), pm.getExternalPort(),
                                pm.getExternalPort() );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Failed to clean port mappings by proxy {}: {}", proxyDto, e.getMessage() );
        }
    }


    private void setupPortMap( ProxyDto proxyDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            for ( PortMapDto portMapDto : proxyDto.getPortMaps() )
            {
                try
                {
                    Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );

                    if ( protocol.isHttpOrHttps() )
                    {
                        if ( portMapDto.getState().equals( PortMapDto.State.CREATING ) || portMapDto.getState().equals(
                                PortMapDto.State.USED ) )
                        {
                            if ( !resourceHost.isPortMappingReserved( protocol, portMapDto.getExternalPort(),
                                    portMapDto.getIpAddr(), portMapDto.getExternalPort(), portMapDto.getDomain() ) )
                            {
                                String sslCertPath = protocol == Protocol.HTTPS ?
                                                     saveSslCertificateToFilesystem( portMapDto, resourceHost ) : null;

                                resourceHost.mapContainerPortToDomain( protocol, portMapDto.getIpAddr(),
                                        portMapDto.getExternalPort(), portMapDto.getExternalPort(),
                                        portMapDto.getDomain(), sslCertPath, LoadBalancing.ROUND_ROBIN,
                                        portMapDto.isSslBackend() );
                            }

                            portMapDto.setState( PortMapDto.State.USED );
                        }
                        else if ( portMapDto.getState().equals( PortMapDto.State.DESTROYING )
                                || portMapDto.getState().equals( PortMapDto.State.DELETED ) )
                        {
                            resourceHost.removeContainerPortDomainMapping( protocol, portMapDto.getIpAddr(),
                                    portMapDto.getExternalPort(), portMapDto.getExternalPort(),
                                    portMapDto.getDomain() );

                            portMapDto.setState( PortMapDto.State.DELETED );
                        }
                    }
                    else
                    {
                        if ( portMapDto.getState().equals( PortMapDto.State.CREATING ) || portMapDto.getState().equals(
                                PortMapDto.State.USED ) )
                        {
                            if ( !resourceHost.isPortMappingReserved( protocol, portMapDto.getExternalPort(),
                                    portMapDto.getIpAddr(), portMapDto.getExternalPort(), portMapDto.getDomain() ) )
                            {
                                resourceHost.mapContainerPort( protocol, portMapDto.getIpAddr(),
                                        portMapDto.getExternalPort(), portMapDto.getExternalPort() );
                            }

                            portMapDto.setState( PortMapDto.State.USED );
                        }
                        else if ( portMapDto.getState().equals( PortMapDto.State.DESTROYING )
                                || portMapDto.getState().equals( PortMapDto.State.DELETED ) )
                        {
                            resourceHost.removeContainerPortMapping( protocol, portMapDto.getIpAddr(),
                                    portMapDto.getExternalPort(), portMapDto.getExternalPort() );

                            portMapDto.setState( PortMapDto.State.DELETED );
                        }
                    }
                }
                catch ( Exception e )
                {
                    portMapDto.setState( PortMapDto.State.ERROR );
                    portMapDto.setErrorLog( e.getMessage() );
                    log.error( "Failed to setup port mapping {}: {}", portMapDto, e );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error during setting up port mappings: {}", e.getMessage() );
        }
    }


    private String saveSslCertificateToFilesystem( PortMapDto portMapDto, ResourceHost rh ) throws CommandException
    {
        if ( StringUtils.isBlank( portMapDto.getSslCertPem() ) )
        {
            return null;
        }

        String fileName = String.format( "%s-%d-%d", portMapDto.getDomain(), portMapDto.getExternalPort(),
                portMapDto.getInternalPort() );

        String sslCertPath = "/tmp/" + fileName + ".pem";

        rh.execute(
                new RequestBuilder( String.format( "echo \"%s\" > %s", portMapDto.getSslCertPem(), sslCertPath ) ) );

        return sslCertPath;
    }


    private void joinEnvironmentP2PSwarm( ProxyDto proxyDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            resourceHost.joinP2PSwarmDHCP( proxyDto.getP2pIfaceName(), proxyDto.getP2pHash(), proxyDto.getP2SecretKey(),
                    proxyDto.getP2pSecretTTL() );

            for ( final P2PConnection p2PConnection : resourceHost.getP2PConnections().getConnections() )
            {
                if ( p2PConnection.getHash().equals( proxyDto.getP2pHash() ) )
                {
                    proxyDto.setP2pIpAddr( p2PConnection.getIp() );
                    break;
                }
            }

            proxyDto.setState( ProxyDto.State.READY );
        }
        catch ( Exception e )
        {
            proxyDto.setState( ProxyDto.State.FAILED );
            proxyDto.setLogs( e.getMessage() );
            log.error( e.getMessage() );
        }
    }


    private void sendDataToBazaar( ProxyDto proxyDto, String link )
    {
        RestResult<Object> result = restClient.post( link, proxyDto );

        if ( !result.isSuccess() )
        {
            log.error( "Error to send  data to Bazaar: " + result.getError() );
        }
        else
        {
            log.info( "Sent Data to Bazaar Success" );
        }
    }
}
