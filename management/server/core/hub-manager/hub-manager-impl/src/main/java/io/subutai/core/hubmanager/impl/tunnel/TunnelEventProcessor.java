package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TunnelInfoDto;


public class TunnelEventProcessor implements Runnable
{

    private static final Logger LOG = LoggerFactory.getLogger( TunnelEventProcessor.class );

    private static final String REST_TUNNEL_URL = "/rest/v1/tunnel/update/";
    private static final String TUNNEL_LIST_CMD = "subutai tunnel list | grep 8443";
    private static String OPENED_IP_PORT;

    private PeerManager peerManager;
    private ConfigManager configManager;
    private HubManager manager;


    public TunnelEventProcessor( final HubManager integration, PeerManager peerManager,
                                 ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
        this.manager = integration;
    }


    @Override
    public void run()
    {
        if ( manager.getRegistrationState() )
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
                Set<String> logs = Sets.newHashSet();

                TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
                tunnelInfoDto.setTunnelStatus( null );
                TunnelHelper.updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto,
                        configManager );

                logs.add( "Executed: " + TUNNEL_LIST_CMD + " |  Result: " + result.getStdErr() );
                TunnelHelper.sendLogs( logs, configManager );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            LOG.error( e.getMessage() );
        }
    }


    private void sendDataToHub( Map<Long, String> map )
    {
        String result = "";
        for ( long key : map.keySet() )
        {
            result = map.get( key );
        }
        OPENED_IP_PORT = result;

        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
        String[] data = result.split( ":" );
        tunnelInfoDto.setOpenedIp( data[0] );
        tunnelInfoDto.setOpenedPort( data[1] );

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
