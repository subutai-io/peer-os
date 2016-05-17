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


public class TunnelEventProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String REST_TUNNEL_URL = "/rest/v1/tunnel/update/";
    private static final String TUNNEL_LIST_CMD = "subutai tunnel list | grep 8443";
    private static String OPENED_IP_PORT;

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
                Map<Long, String> map = parseResult( result.getStdOut() );

                if ( OPENED_IP_PORT == null || !map.containsValue( OPENED_IP_PORT ) )
                {
                    sendDataToHub( map );
                }
            }

            if ( !result.hasSucceeded() && !result.getStdErr().isEmpty() )
            {
                TunnelHelper.sendError( REST_TUNNEL_URL + configManager.getPeerId(),
                        "Executed: " + TUNNEL_LIST_CMD + " |  Result: " + result.getStdErr(), configManager );
            }
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( REST_TUNNEL_URL + configManager.getPeerId(), e.getMessage(), configManager );
            log.error( e.getMessage() );
        }
    }


    private void sendDataToHub( Map<Long, String> map )
    {
        String result = "";

        if ( map.containsKey( -1L ) )
        {
            result = map.get( -1L );
        }

        else
        {
            for ( long key : map.keySet() )
            {
                result = map.get( key );
            }
        }

        OPENED_IP_PORT = result;

        TunnelInfoDto tunnelInfoDto =
                TunnelHelper.parseResult( REST_TUNNEL_URL + configManager.getPeerId(), result, configManager );

        Response response = TunnelHelper
                .updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto, configManager );

        if ( response.getStatus() != HttpStatus.SC_OK && response.getStatus() != 204 )
        {
            OPENED_IP_PORT = null;
        }
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
