package io.subutai.core.bazaarmanager.impl.processor.port_map;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.bazaar.share.dto.domain.ContainerPortMapDto;
import io.subutai.bazaar.share.dto.domain.PortMapDto;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;

import static java.lang.String.format;


public class ContainerPortMapProcessor implements StateLinkProcessor
{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final Set<String> LINKS_IN_PROGRESS = Sets.newConcurrentHashSet();
    private static final Set<PortMapDto> PORT_CACHE = Sets.newConcurrentHashSet();

    private Context ctx;


    public ContainerPortMapProcessor( final Context ctx )
    {
        this.ctx = ctx;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws BazaarManagerException
    {

        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "/ports/" ) )
            {
                processLink( stateLink );
            }
        }


        return false;
    }


    private void processLink( String stateLink )
    {
        log.info( "Link process - START: {}", stateLink );

        if ( LINKS_IN_PROGRESS.contains( stateLink ) )
        {
            log.info( "This link is in progress: {}", stateLink );

            return;
        }

        LINKS_IN_PROGRESS.add( stateLink );

        try
        {

            RestResult<ContainerPortMapDto> restResult = ctx.restClient.get( stateLink, ContainerPortMapDto.class );

            if ( !restResult.isSuccess() )
            {
                log.error( "Could not get port map data from Bazaar" );
            }

            ContainerPortMapDto containerPortMapDto = restResult.getEntity();

            for ( PortMapDto portMapDto : containerPortMapDto.getContainerPorts() )
            {
                handlePortMap( portMapDto );
            }

            RestResult<Object> restRes = ctx.restClient
                    .post( format( "/rest/v1/environments/%s/ports/map", containerPortMapDto.getEnvironmentSSId() ),
                            containerPortMapDto );

            log.info(
                    !restRes.isSuccess() ? "Could not send port map data to Bazaar" : "Sent port map data to Bazaar" );
        }
        catch ( Exception e )
        {
            log.error( "*********", e );
        }
        finally
        {
            LINKS_IN_PROGRESS.remove( stateLink );
            PORT_CACHE.clear();
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
                DestroyPortMap destroyPortMap = new DestroyPortMap( ctx );
                destroyPortMap.deleteMap( portMapDto );
                break;
            case ERROR:
                break;
            case USED:
                break;
            default:
                log.error( "Port map state is unknown ={} ", portMapDto.getState() );
        }
    }


    private void createMap( PortMapDto portMapDto )
    {
        try
        {
            if ( portMapDto.getState() == PortMapDto.State.USED )
            {
                return;
            }

            if ( PORT_CACHE.contains( portMapDto ) )
            {
                return;
            }

            PORT_CACHE.add( portMapDto );


            ContainerHost containerHost = ctx.localPeer.getContainerHostById( portMapDto.getContainerSSId() );

            ResourceHost resourceHost =
                    ctx.localPeer.getResourceHostById( containerHost.getResourceHostId().toString() );

            PortMapUtil.mapPortToIp( portMapDto, resourceHost, containerHost.getIp(), portMapDto.getInternalPort() );

            if ( !resourceHost.isManagementHost() && !portMapDto.isProxied() )
            {
                // Container resides on additional RH, that's why we need to forward traffic from MH to RH.
                // On MH external port should be forwarded to same external port of RH, then on RH real
                // 'external port -> internal port' mapping should occur.

                ResourceHost mngHost = ctx.localPeer.getManagementHost();

                PortMapUtil.mapPortToIp( portMapDto, mngHost, resourceHost.getIp(), portMapDto.getExternalPort() );
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
