package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class TunnelProcessor implements StateLinkProcessor
{
    private final Logger LOG = LoggerFactory.getLogger( getClass() );
    private static final String CREATE_TUNNEL_COMMAND = "subutai tunnel add %s:%s %s -g";
    private static final String DELETE_TUNNEL_COMMAND = "subutai tunnel del %s:%s";

    private PeerManager peerManager;
    private ConfigManager configManager;


    public TunnelProcessor( PeerManager peerManager, ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public void processStateLinks( final Set<String> stateLinks ) throws HubPluginException
    {
        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "tunnel" ) )
            {
                processLink( stateLink );
            }
        }
    }


    private void processLink( String stateLink )
    {
        TunnelInfoDto tunnelInfoDto = getData( stateLink );

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


    private void deleteTunnel( final String stateLink, final TunnelInfoDto tunnelInfoDto )
    {
        ResourceHost resourceHost = null;
        try
        {
            resourceHost = peerManager.getLocalPeer().getManagementHost();
        }
        catch ( HostNotFoundException e )
        {
            e.printStackTrace();
        }

        CommandResult result = TunnelHelper.execute( resourceHost,
                String.format( DELETE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen() ) );

        if ( !result.hasSucceeded() )
        {
            String errorLog = String.format( "Executed: " + CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(),
                    tunnelInfoDto.getPortToOpen(), getTunnelLifetime( tunnelInfoDto ) ) + " |  Result: " + result
                    .getStdErr();

            sendError( stateLink, errorLog );
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

            LOG.debug( "Tunnel output: " + result.getStdOut() );

            if ( result.hasSucceeded() )
            {
                String[] data = result.getStdOut().split( ":" );
                tunnelInfoDto.setOpenedIp( data[0] );
                tunnelInfoDto.setOpenedPort( data[1] );

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
            else
            {
                String errorLog = String.format( "Executed: " + CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(),
                        tunnelInfoDto.getPortToOpen(), tunnelLifeTime ) + " |  Result: " + result.getStdErr();

                sendError( stateLink, errorLog );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    private void sendError( final String stateLink, String errorLog )
    {

        Set<String> logs = Sets.newHashSet();
        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
        tunnelInfoDto.setTunnelStatus( null );

        TunnelHelper.updateTunnelStatus( stateLink, tunnelInfoDto, configManager );
        logs.add( errorLog );

        TunnelHelper.sendLogs( logs, configManager );
    }


    private String getTunnelLifetime( TunnelInfoDto tunnelInfoDto )
    {
        return tunnelInfoDto.getTtl() < 0 ? "" : tunnelInfoDto.getTtl().toString();
    }


    private TunnelInfoDto getData( String link )
    {
        LOG.debug( "Getting tunnel data from Hub: {}", link );

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
            LOG.error( "Error to get TunnelInfoDto data from Hub: ", e );

            return null;
        }
    }
}
