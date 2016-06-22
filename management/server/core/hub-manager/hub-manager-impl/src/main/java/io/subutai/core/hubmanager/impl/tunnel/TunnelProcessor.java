package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;

import static io.subutai.hub.share.dto.TunnelInfoDto.TunnelStatus.READY;


// TODO: Replace WebClient with HubRestClient.
public class TunnelProcessor implements StateLinkProcessor
{
    private final Logger LOG = LoggerFactory.getLogger( getClass() );
    public static final String CREATE_TUNNEL_COMMAND = "subutai tunnel add %s:%s %s -g";
    public static final String DELETE_TUNNEL_COMMAND = "subutai tunnel del %s:%s";

    private PeerManager peerManager;
    private ConfigManager configManager;


    public TunnelProcessor( PeerManager peerManager, ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
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


    private void processLink( String stateLink )
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
            }
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
            LOG.error( e.getMessage() );
        }

        CommandResult result = TunnelHelper.execute( resourceHost,
                String.format( DELETE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen() ) );

        if ( !result.hasSucceeded() )
        {
            String errorLog = String.format( "Executed: " + CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(),
                    tunnelInfoDto.getPortToOpen(), getTunnelLifetime( tunnelInfoDto ) ) + " |  Result: " + result
                    .getStdErr();

            TunnelHelper.sendError( stateLink, errorLog, configManager );
        }
    }


    private void createTunnel( String stateLink, TunnelInfoDto tunnelInfoDto )
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();
            String tunnelLifeTime = getTunnelLifetime( tunnelInfoDto );

            CommandResult result = TunnelHelper.execute( resourceHost,
                    String.format( CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            tunnelLifeTime ) );

            if ( result.hasSucceeded() )
            {
                tunnelInfoDto = TunnelHelper.parseResult( stateLink, result.getStdOut(), configManager );

                if ( tunnelInfoDto != null )
                {
                    tunnelInfoDto.setTunnelStatus( READY );
                    Response response = TunnelHelper.updateTunnelStatus( stateLink, tunnelInfoDto, configManager );

                    if ( response.getStatus() == HttpStatus.SC_OK )
                    {
                        LOG.debug( "Tunnel peer data successfully sent to hub" );
                    }
                    else
                    {
                        LOG.error( "Tunnel peer data was not successfully sent to hub" );
                    }
                }
            }
            else
            {
                String errorLog = String.format( "Executed: " + CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(),
                        tunnelInfoDto.getPortToOpen(), tunnelLifeTime ) + " |  Result: " + result.getStdErr();

                TunnelHelper.sendError( stateLink, errorLog, configManager );
            }
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( stateLink, e.getMessage(), configManager );
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

            LOG.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( "Error to get tunnel  data from Hub: HTTP {} - {}", res.getStatus(),
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
            LOG.error( e.getMessage() );
            return null;
        }
    }
}
