package io.subutai.core.hubmanager.impl.tunnel;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;

import static java.lang.String.format;

import static io.subutai.hub.share.dto.TunnelInfoDto.TunnelStatus.READY;


// TODO: Replace WebClient with HubRestClient.
public class TunnelProcessor implements StateLinkProcessor
{
    public static final String CREATE_TUNNEL_COMMAND = "subutai tunnel add %s:%s %s -g";

    public static final String DELETE_TUNNEL_COMMAND = "subutai tunnel del %s:%s";

    private static final HashSet<String> LINKS_IN_PROGRESS = new HashSet<>();

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private PeerManager peerManager;

    private ConfigManager configManager;


    public TunnelProcessor( PeerManager peerManager, ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public synchronized boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
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


    private void processLink( String stateLink ) throws HubManagerException
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
            throw new HubManagerException( e );
        }
        finally
        {
            log.info( "Link process - END: {}", stateLink );

            LINKS_IN_PROGRESS.remove( stateLink );
        }
    }


    private void deleteTunnel( final String stateLink, final TunnelInfoDto tunnelInfoDto )
    {
        ResourceHost resourceHost = null;
        try
        {
            resourceHost = peerManager.getLocalPeer().getManagementHost();
        }
        catch ( HostNotFoundException e )
        {
            TunnelHelper.sendError( stateLink, e.getMessage(), configManager );
            log.error( e.getMessage() );
        }

        CommandResult result = TunnelHelper.execute( resourceHost,
                format( DELETE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen() ) );

        Preconditions.checkNotNull( result );

        if ( !result.hasSucceeded() )
        {
            String errorLog = "Executed: " +
                    format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            getTunnelLifetime( tunnelInfoDto ) ) + " |  Result: " + result.getStdErr();

            TunnelHelper.sendError( stateLink, errorLog, configManager );
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

            CommandResult result =
                    getOpenedTunnelData( resourceHost, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen() );

            if ( result == null )
            {
                result = TunnelHelper.execute( resourceHost,
                        format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                                tunnelLifeTime ) );
            }

            parseResult( stateLink, result, tunnelInfoDto, tunnelLifeTime );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
            TunnelHelper.sendError( stateLink, e.getMessage(), configManager );
        }
    }


    private CommandResult getOpenedTunnelData( ResourceHost resourceHost, String ip, String port )
    {
        CommandResult result =
                TunnelHelper.execute( resourceHost, format( TunnelEventProcessor.TUNNEL_LIST_CMD, ip, port ) );

        Preconditions.checkNotNull( result );

        if ( !result.hasSucceeded() )
        {
            return null;
        }

        return result;
    }


    private void parseResult( String stateLink, CommandResult result, TunnelInfoDto tunnelInfoDto,
                              String tunnelLifeTime )
    {
        if ( result.hasSucceeded() )
        {
            tunnelInfoDto = TunnelHelper.parseResult( stateLink, result.getStdOut(), configManager, tunnelInfoDto );

            if ( tunnelInfoDto != null )
            {
                tunnelInfoDto.setTunnelStatus( READY );
                TunnelEventProcessor.setOpenPort( result.getStdOut() );
                Response response = TunnelHelper.updateTunnelStatus( stateLink, tunnelInfoDto, configManager );

                Preconditions.checkNotNull( response );

                if ( response.getStatus() == HttpStatus.SC_OK || response.getStatus() == 204 )
                {
                    log.info( "Tunnel peer data successfully sent to hub" );
                }
                else
                {
                    log.error( "Tunnel peer data was not successfully sent to hub" );
                }
            }
        }
        else
        {
            String errorLog = "Executed: " +
                    format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            tunnelLifeTime ) + " |  Result: " + result.getStdErr();

            TunnelHelper.sendError( stateLink, errorLog, configManager );
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
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get tunnel  data from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, TunnelInfoDto.class );
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( link, e.getMessage(), configManager );
            log.error( e.getMessage() );
            return null;
        }
    }
}
