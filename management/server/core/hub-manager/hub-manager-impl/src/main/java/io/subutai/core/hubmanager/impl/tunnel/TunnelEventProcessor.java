package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;

import static io.subutai.hub.share.dto.TunnelInfoDto.TunnelStatus.READY;


public class TunnelEventProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String REST_TUNNEL_URL = "/rest/v1/tunnel/update/";
    private static final String REST_GET_TUNNEL_DATA_URL = "/rest/v1/tunnel/%s";

    public static final String TUNNEL_LIST_CMD = "subutai tunnel list | grep 8443";
    public static String OPENED_IP_PORT;

    private PeerManager peerManager;

    private ConfigManager configManager;

    private HubManager hubManager;


    public TunnelEventProcessor( final HubManager hubManager, PeerManager peerManager, ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
        this.hubManager = hubManager;
    }


    @Override
    public void run()
    {
        if ( hubManager.isRegistered() )
        {
            startProccess();
        }
    }


    private void startProccess()
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();
            CommandResult result = TunnelHelper.execute( resourceHost, TUNNEL_LIST_CMD );

            if ( result.hasSucceeded() && !result.getStdOut().isEmpty() )
            {
                updateTunnelIpPort( result );
            }
            else
            {
                checkTunnelStateHub( resourceHost );
            }
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( REST_TUNNEL_URL + configManager.getPeerId(), e.getMessage(), configManager );
            log.error( e.getMessage() );
        }
    }


    private void updateTunnelIpPort( final CommandResult result )
    {
        Map<Long, String> map = parseResult( result.getStdOut() );

        if ( OPENED_IP_PORT == null || !map.containsValue( OPENED_IP_PORT ) )
        {
            sendDataToHub( map );
        }
    }


    private void checkTunnelStateHub( ResourceHost resourceHost )
    {
        TunnelInfoDto tunnelInfoDto = TunnelHelper
                .getPeerTunnelState( String.format( REST_GET_TUNNEL_DATA_URL, configManager.getPeerId() ),
                        configManager );

        if ( tunnelInfoDto != null && tunnelInfoDto.getTunnelStatus().equals( READY ) )
        {
            CommandResult resultIpPort = TunnelHelper.execute( resourceHost,
                    String.format( TunnelProcessor.CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(),
                            tunnelInfoDto.getPortToOpen(), "" ) );

            if ( resultIpPort.hasSucceeded() )
            {
                TunnelInfoDto tunnelInfoDto1 = TunnelHelper
                        .parseResult( REST_TUNNEL_URL + configManager.getPeerId(), resultIpPort.getStdOut(),
                                configManager );
                TunnelHelper.updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto1,
                        configManager );
            }
            OPENED_IP_PORT = resultIpPort.getStdOut().replaceAll( "\n", "" );
        }
    }


    private void sendDataToHub( Map<Long, String> map )
    {
        OPENED_IP_PORT = getOptimalIpPort( map );

        TunnelInfoDto tunnelInfoDto =
                TunnelHelper.parseResult( REST_TUNNEL_URL + configManager.getPeerId(), OPENED_IP_PORT, configManager );

        Response response = TunnelHelper
                .updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto, configManager );

        if ( response.getStatus() != HttpStatus.SC_OK && response.getStatus() != 204 )
        {
            OPENED_IP_PORT = null;
        }
    }


    private String getOptimalIpPort( final Map<Long, String> map )
    {
        String ipPort = "";
        if ( map.containsKey( -1L ) )
        {
            ipPort = map.get( -1L );
        }

        else
        {
            for ( long key : map.keySet() )
            {
                ipPort = map.get( key );
            }
        }

        return ipPort;
    }


    private Map<Long, String> parseResult( String result )
    {
        Map<Long, String> tunnelCache = new TreeMap<>();
        String[] ipArray = result.split( "\n" );

        for ( String ipPort : ipArray )
        {
            String[] tunnelData = ipPort.split( " " );
            tunnelCache.put( Long.valueOf( tunnelData[2] ), tunnelData[0] );
        }

        return tunnelCache;
    }
}
