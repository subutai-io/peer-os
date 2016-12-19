package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;

import static java.lang.String.format;

import static io.subutai.hub.share.dto.TunnelInfoDto.TunnelStatus.READY;


public class TunnelEventProcessor extends HubRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String REST_TUNNEL_URL = "/rest/v1/tunnel/update/";
    private static final String REST_GET_TUNNEL_DATA_URL = "/rest/v1/tunnel/%s";

    static final String TUNNEL_LIST_CMD = "subutai tunnel list | grep %s:%s";
    private static String OPENED_IP_PORT;

    private PeerManager peerManager;

    private ConfigManager configManager;


    public TunnelEventProcessor( final HubManager hubManager, final PeerManager peerManager,
                                 final ConfigManager configManager, final RestClient restClient )
    {
        super( hubManager, restClient );

        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public void request()
    {
        startProccess();
    }


    private void startProccess()
    {
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getManagementHost();

            CommandResult result =
                    TunnelHelper.execute( resourceHost, format( TUNNEL_LIST_CMD, "10.10.10.1", "8443" ) );

            Preconditions.checkNotNull( result );

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
                .getPeerTunnelState( format( REST_GET_TUNNEL_DATA_URL, configManager.getPeerId() ), configManager );

        if ( tunnelInfoDto != null && tunnelInfoDto.getTunnelStatus().equals( READY ) )
        {
            CommandResult resultIpPort = TunnelHelper.execute( resourceHost,
                    format( TunnelProcessor.CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            "" ) );

            Preconditions.checkNotNull( resultIpPort );

            if ( resultIpPort.hasSucceeded() )
            {
                tunnelInfoDto = TunnelHelper
                        .parseResult( REST_TUNNEL_URL + configManager.getPeerId(), resultIpPort.getStdOut(),
                                configManager, tunnelInfoDto );

                TunnelHelper.updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto,
                        configManager );
            }
            setOpenPort( resultIpPort.getStdOut().replaceAll( "\n", "" ) );
        }
    }


    static synchronized void setOpenPort( String port )
    {
        OPENED_IP_PORT = port;
    }


    private void sendDataToHub( Map<Long, String> map )
    {
        setOpenPort( getOptimalIpPort( map ) );

        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();

        tunnelInfoDto = TunnelHelper
                .parseResult( REST_TUNNEL_URL + configManager.getPeerId(), OPENED_IP_PORT, configManager,
                        tunnelInfoDto );

        Response response = TunnelHelper
                .updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto, configManager );

        Preconditions.checkNotNull( response );

        if ( response.getStatus() != HttpStatus.SC_OK && response.getStatus() != 204 )
        {
            setOpenPort( null );
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
            for ( Map.Entry<Long, String> entry : map.entrySet() )
            {
                ipPort = entry.getValue();
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
