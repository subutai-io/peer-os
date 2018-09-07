package io.subutai.core.bazaarmanager.impl.tunnel;


import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.TunnelInfoDto;

import static java.lang.String.format;

import static io.subutai.bazaar.share.dto.TunnelInfoDto.TunnelStatus.READY;


public class TunnelEventProcessor extends BazaarRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String REST_TUNNEL_URL = "/rest/v1/tunnel/update/";
    private static final String REST_GET_TUNNEL_DATA_URL = "/rest/v1/tunnel/%s";

    static final String TUNNEL_LIST_CMD = "subutai tunnel list | grep %s:%s";
    private static String OPENED_IP_PORT;

    private PeerManager peerManager;

    private ConfigManager configManager;


    public TunnelEventProcessor( final BazaarManager bazaarManager, final PeerManager peerManager,
                                 final ConfigManager configManager, final RestClient restClient )
    {
        super( bazaarManager, restClient );

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
                    resourceHost.execute( new RequestBuilder( format( TUNNEL_LIST_CMD, "10.10.10.1", "8443" ) ) );

            Preconditions.checkNotNull( result );

            if ( result.hasSucceeded() && !result.getStdOut().isEmpty() )
            {
                updateTunnelIpPort( result );
            }
            else
            {
                checkTunnelStateBazaar( resourceHost );
            }
        }
        catch ( Exception e )
        {
            TunnelHelper.sendError( REST_TUNNEL_URL + configManager.getPeerId(), e.getMessage(), restClient );
            log.error( e.getMessage() );
        }
    }


    private void updateTunnelIpPort( final CommandResult result )
    {
        TunnelInfoDto tunnelInfoDto = TunnelHelper
                .getPeerTunnelState( format( REST_GET_TUNNEL_DATA_URL, configManager.getPeerId() ), restClient );

        if ( tunnelInfoDto != null && tunnelInfoDto.getTunnelStatus().equals( READY ) )
        {
            Map<Long, String> map = parseResult( result.getStdOut() );

            if ( OPENED_IP_PORT == null || !map.containsValue( OPENED_IP_PORT ) )
            {
                sendDataToBazaar( map );
            }
        }
    }


    private void checkTunnelStateBazaar( ResourceHost resourceHost ) throws CommandException
    {
        TunnelInfoDto tunnelInfoDto = TunnelHelper
                .getPeerTunnelState( format( REST_GET_TUNNEL_DATA_URL, configManager.getPeerId() ), restClient );

        if ( tunnelInfoDto != null && tunnelInfoDto.getTunnelStatus().equals( READY ) )
        {
            CommandResult resultIpPort = resourceHost.execute( new RequestBuilder(
                    format( TunnelProcessor.CREATE_TUNNEL_COMMAND, tunnelInfoDto.getIp(), tunnelInfoDto.getPortToOpen(),
                            "" ) ) );

            Preconditions.checkNotNull( resultIpPort );

            if ( resultIpPort.hasSucceeded() )
            {
                tunnelInfoDto = TunnelHelper.parseResult( resultIpPort.getStdOut(), tunnelInfoDto );

                TunnelHelper
                        .updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto, restClient );
            }
            setOpenPort( resultIpPort.getStdOut().replaceAll( "\n", "" ) );
        }
    }


    static synchronized void setOpenPort( String port )
    {
        OPENED_IP_PORT = port;
    }


    private void sendDataToBazaar( Map<Long, String> map )
    {
        setOpenPort( getOptimalIpPort( map ) );

        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();

        tunnelInfoDto = TunnelHelper.parseResult( OPENED_IP_PORT, tunnelInfoDto );

        RestResult<Object> response = TunnelHelper
                .updateTunnelStatus( REST_TUNNEL_URL + configManager.getPeerId(), tunnelInfoDto, restClient );

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
        String[] ipArray = result.split( "\\n" );

        for ( String ipPort : ipArray )
        {
            String[] tunnelData = ipPort.split( "\\t" );
            tunnelCache.put( Long.valueOf( tunnelData[2] ), tunnelData[0] );
        }

        return tunnelCache;
    }
}
