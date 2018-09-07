package io.subutai.core.bazaarmanager.impl.tunnel;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.TunnelInfoDto;

import static java.lang.String.format;

import static io.subutai.bazaar.share.dto.TunnelInfoDto.TunnelStatus.READY;


public class TunnelProcessor implements StateLinkProcessor
{
    public static final String CREATE_TUNNEL_COMMAND = "subutai tunnel add %s:%s %s";

    private static final String DELETE_TUNNEL_COMMAND = "subutai tunnel del %s:%s";

    private static final Set<String> LINKS_IN_PROGRESS = Sets.newConcurrentHashSet();

    private final Logger log = LoggerFactory.getLogger( getClass() );
    private final RestClient restClient;

    private PeerManager peerManager;


    public TunnelProcessor( PeerManager peerManager, RestClient restClient )
    {
        this.peerManager = peerManager;
        this.restClient = restClient;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws BazaarManagerException
    {
        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "tunnel" ) )
            {
                processLink( stateLink );
            }
        }

        return false;
    }


    private void processLink( String stateLink ) throws BazaarManagerException
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
            TunnelInfoDto tunnelInfoDto = getData( stateLink );

            if ( tunnelInfoDto != null )
            {
                switch ( tunnelInfoDto.getTunnelStatus() )
                {
                    case PENDING:
                        createTunnel( stateLink, tunnelInfoDto );
                        break;

                    case DELETE:
                        deleteTunnel( stateLink, tunnelInfoDto );
                        break;
                    default:
                        log.info( "Requested {}", tunnelInfoDto.getTunnelStatus() );
                        break;
                }
            }
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
        finally
        {
            log.info( "Link process - END: {}", stateLink );

            LINKS_IN_PROGRESS.remove( stateLink );
        }
    }


    private void deleteTunnel( final String stateLink, final TunnelInfoDto tunnelInfoDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            CommandResult result = resourceHost.execute( new RequestBuilder(
                    format( DELETE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen() ) ) );

            if ( !result.hasSucceeded() )
            {
                String errorLog = "Executed: " + format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(),
                        tunnelInfoDto.getPortToOpen(), getTunnelLifetime( tunnelInfoDto ) ) + " |  Result: " + result
                        .getStdErr();

                TunnelHelper.sendError( stateLink, errorLog, restClient );
            }
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( stateLink, e.getMessage(), restClient );
            log.error( e.getMessage() );
        }
    }


    private void createTunnel( String stateLink, TunnelInfoDto tunnelInfoDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            if ( tunnelInfoDto.getContainerId() != null && !tunnelInfoDto.getContainerId().isEmpty() )
            {
                resourceHost =
                        peerManager.getLocalPeer().getResourceHostByContainerId( tunnelInfoDto.getContainerId() );
            }


            String tunnelLifeTime = getTunnelLifetime( tunnelInfoDto );

            CommandResult result = resourceHost.execute( new RequestBuilder(
                    format( TunnelEventProcessor.TUNNEL_LIST_CMD, tunnelInfoDto.getIp(),
                            tunnelInfoDto.getPortToOpen() ) ) );

            if ( !result.hasSucceeded() )
            {
                result = resourceHost.execute( new RequestBuilder(
                        format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                                tunnelLifeTime ) ) );
            }

            parseResult( stateLink, result, tunnelInfoDto, tunnelLifeTime );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
            TunnelHelper.sendError( stateLink, e.getMessage(), restClient );
        }
    }


    private void parseResult( String stateLink, CommandResult result, TunnelInfoDto tunnelInfoDto,
                              String tunnelLifeTime )
    {
        if ( result.hasSucceeded() )
        {
            tunnelInfoDto = TunnelHelper.parseResult( result.getStdOut(), tunnelInfoDto );

            if ( tunnelInfoDto != null )
            {
                tunnelInfoDto.setTunnelStatus( READY );
                TunnelEventProcessor.setOpenPort( result.getStdOut() );
                RestResult<Object> response = TunnelHelper.updateTunnelStatus( stateLink, tunnelInfoDto, restClient );

                Preconditions.checkNotNull( response );

                if ( response.getStatus() == HttpStatus.SC_OK || response.getStatus() == 204 )
                {
                    log.info( "Tunnel peer data successfully sent to Bazaar" );
                }
                else
                {
                    log.error( "Tunnel peer data was not successfully sent to Bazaar" );
                }
            }
        }
        else
        {
            String errorLog =
                    "Executed: " + format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            tunnelLifeTime ) + " |  Result: " + result.getStdErr();

            TunnelHelper.sendError( stateLink, errorLog, restClient );
        }
    }


    private String getTunnelLifetime( TunnelInfoDto tunnelInfoDto )
    {
        return tunnelInfoDto.getTtl() < 0 ? "" : tunnelInfoDto.getTtl().toString();
    }


    private TunnelInfoDto getData( String link )
    {
        try
        {
            RestResult<TunnelInfoDto> restResult = restClient.get( link, TunnelInfoDto.class );

            log.debug( "Response: HTTP {} - {}", restResult.getStatus(), restResult.getReasonPhrase() );

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get tunnel  data from Bazaar: HTTP {} - {}", restResult.getStatus(),
                        restResult.getError() );

                return null;
            }

            return restResult.getEntity();
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( link, e.getMessage(), restClient );
            log.error( e.getMessage() );
            return null;
        }
    }
}
