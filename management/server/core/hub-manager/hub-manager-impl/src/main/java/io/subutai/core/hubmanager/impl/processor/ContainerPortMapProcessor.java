package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.Protocol;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.domain.ContainerPortMapDto;
import io.subutai.hub.share.dto.domain.PortMapDto;
import io.subutai.hub.share.dto.domain.ReservedPortMapping;

import static java.lang.String.format;


public class ContainerPortMapProcessor implements StateLinkProcessor
{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final HashSet<String> LINKS_IN_PROGRESS = new HashSet<>();

//    private PeerManager peerManager;
//
//    private HubRestClient restClient;

    private Context ctx;


    public ContainerPortMapProcessor( PeerManager peerManager, HubRestClient restClient )
    {
//        this.peerManager = peerManager;
//        this.restClient = restClient;
    }


    public ContainerPortMapProcessor( final Context ctx )
    {
        this.ctx = ctx;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {

        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "port" ) )
            {
                processLink( stateLink );
            }
        }


        return false;
    }


    private void processLink( String stateLink )
    {
        try
        {

            log.info( "Link process - START: {}", stateLink );

            if ( LINKS_IN_PROGRESS.contains( stateLink ) )
            {
                log.info( "This link is in progress: {}", stateLink );

                return;
            }

            LINKS_IN_PROGRESS.add( stateLink );

            RestResult<ContainerPortMapDto> restResult = ctx.restClient.get( stateLink, ContainerPortMapDto.class );

            if ( !restResult.isSuccess() )
            {
                log.error( "Could not get port map data from HUB" );
            }

            ContainerPortMapDto containerPortMapDto = restResult.getEntity();

            for ( PortMapDto portMapDto : containerPortMapDto.getContainerPorts() )
            {
                handlePortMap( portMapDto );
            }

            RestResult<Object> restRes = ctx.restClient
                    .post( format( "/rest/v1/environments/%s/ports/map", containerPortMapDto.getEnvironmentSSId() ),
                            containerPortMapDto );

            if ( !restRes.isSuccess() )
            {
                log.error( "Could not send port map data to HUB" );
            }
            else
            {
                log.info( "Sent port map data to HUB" );
            }
        }
        catch ( Exception e )
        {
            log.error( "*********", e );
        }
        finally
        {
            LINKS_IN_PROGRESS.remove( stateLink );
        }
    }


    private void handlePortMap( PortMapDto portMapDto )
    {
        switch ( portMapDto.getState() )
        {
            case CREATING:
                createMap( portMapDto );
                break;
            case DESTROYING:
                deleteMap( portMapDto );
                break;
            case ERROR:
                break;
            default:
                log.error( "Port map state is unknown ={} ", portMapDto.getState() );
        }
    }


    private void deleteMap( final PortMapDto portMapDto )
    {
        try
        {
            ContainerHost containerHost = ctx.localPeer.getContainerHostById( portMapDto.getContainerSSId() );

            ResourceHost resourceHost =
                    ctx.localPeer.getResourceHostById( containerHost.getResourceHostId().toString() );

            Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );


            if ( protocol == Protocol.HTTP || protocol == Protocol.HTTPS )
            {
                resourceHost.removeContainerPortDomainMapping( protocol, containerHost.getIp(),
                        portMapDto.getInternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain() );
            }
            else
            {
                resourceHost.removeContainerPortMapping( protocol, containerHost.getIp(), portMapDto.getInternalPort(),
                        portMapDto.getExternalPort() );
            }

            // if it's RH, remove port mapping from MH too
            if ( !resourceHost.isManagementHost() )
            {
                boolean mappingIsInUseOnRH = false;

                // check if port mapping on MH is not used for other container on this RH
                for ( final ReservedPortMapping mapping : resourceHost.getReservedPortMappings() )
                {
                    if ( mapping.getProtocol() == portMapDto.getProtocol()
                            && mapping.getExternalPort() == portMapDto.getExternalPort() )
                    {
                        mappingIsInUseOnRH = true;
                        break;
                    }
                }


                if ( !mappingIsInUseOnRH )
                {
                    String rhIpAddr = resourceHost.getInterfaceByName( "wan" ).getIp();

                    if ( protocol == Protocol.HTTP || protocol == Protocol.HTTPS )
                    {
                        ctx.localPeer.getManagementHost().removeContainerPortDomainMapping( protocol, rhIpAddr,
                                portMapDto.getExternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain() );
                    }
                    else
                    {
                        ctx.localPeer.getManagementHost().removeContainerPortMapping( protocol, rhIpAddr,
                                portMapDto.getExternalPort(), portMapDto.getExternalPort() );
                    }
                }
            }

            portMapDto.setState( PortMapDto.State.DESTROYING );
        }
        catch ( Exception e )
        {
            portMapDto.setState( PortMapDto.State.ERROR );
            portMapDto.setErrorLog( e.getMessage() );
            log.error( "*********", e );
        }
    }


    private void createMap( PortMapDto portMapDto )
    {
        try
        {
            ContainerHost containerHost = ctx.localPeer.getContainerHostById( portMapDto.getContainerSSId() );

            ResourceHost resourceHost =
                    ctx.localPeer.getResourceHostById( containerHost.getResourceHostId().toString() );

            Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );

            if ( resourceHost.isManagementHost() )
            {
                if ( protocol != Protocol.HTTP && protocol != Protocol.HTTPS )
                {
                    resourceHost.mapContainerPort( protocol, containerHost.getIp(), portMapDto.getInternalPort(),
                            portMapDto.getExternalPort() );
                }
                else
                {
                    resourceHost.mapContainerPortToDomain( protocol, containerHost.getIp(),
                            portMapDto.getInternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain(), null,
                            LoadBalancing.ROUND_ROBIN );
                }
            }
            else
            {
                // Container resides on additional RH, that's why we need to forward traffic from MH to RH.
                // On MH external port should be forwarded to same external port of RH, then on RH real
                // 'external port -> internal port' mapping should occur.

                ResourceHost mngHost = ctx.localPeer.getManagementHost();
                String rhIpAddr = resourceHost.getInterfaceByName( "wan" ).getIp();

                if ( protocol != Protocol.HTTP && protocol != Protocol.HTTPS )
                {
                    if ( !mngHost.isPortMappingReserved( protocol, portMapDto.getExternalPort(), rhIpAddr,
                            portMapDto.getInternalPort() ) )
                    {
                        mngHost.mapContainerPort( protocol, rhIpAddr, portMapDto.getExternalPort(),
                                portMapDto.getExternalPort() );
                    }

                    resourceHost.mapContainerPort( protocol, containerHost.getIp(), portMapDto.getInternalPort(),
                            portMapDto.getExternalPort() );
                }
                else
                {
                    if ( !mngHost.isPortMappingReserved( protocol, portMapDto.getExternalPort(), rhIpAddr,
                            portMapDto.getInternalPort() ) )
                    {
                        mngHost.mapContainerPortToDomain( protocol, rhIpAddr, portMapDto.getExternalPort(),
                                portMapDto.getExternalPort(), portMapDto.getDomain(), null,
                                LoadBalancing.ROUND_ROBIN );
                    }

                    resourceHost.mapContainerPortToDomain( protocol, containerHost.getIp(),
                            portMapDto.getInternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain(),
                            null, LoadBalancing.ROUND_ROBIN );
                }
            }

            portMapDto.setState( PortMapDto.State.USED );
        }
        catch ( Exception e )
        {
            portMapDto.setState( PortMapDto.State.ERROR );
            portMapDto.setErrorLog( e.getMessage() );
            log.error( "*********", e );
        }
    }
}
