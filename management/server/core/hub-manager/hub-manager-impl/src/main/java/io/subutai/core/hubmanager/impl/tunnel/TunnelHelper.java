package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.hub.share.common.util.DtoConverter;
import io.subutai.hub.share.dto.CommonDto;
import io.subutai.hub.share.dto.TunnelInfoDto;

import static io.subutai.hub.share.dto.TunnelInfoDto.TunnelStatus.ERROR;


public class TunnelHelper
{
    private static final Logger LOG = LoggerFactory.getLogger( TunnelHelper.class );


    private static final String DELETE_TUNNEL_COMMAND = "subutai tunnel del %s";
    private static final String GET_OPENED_TUNNELS_FOR_IP_COMMAND = "subutai tunnel list | grep %s | awk '{print $2}'";


    private TunnelHelper()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static CommandResult execute( ResourceHost resourceHost, String cmd )
    {
        boolean exec = true;
        int tryCount = 0;
        CommandResult result = null;
        while ( exec )
        {
            tryCount++;
            exec = tryCount <= 3;
            try
            {
                result = resourceHost.execute( new RequestBuilder( cmd ) );

                if ( result.hasSucceeded() )
                {
                    exec = false;
                    break;
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage() );
            }

            TaskUtil.sleep( 5000 );
        }

        return result;
    }


    static void sendError( String link, String errorLog, RestClient restClient )
    {
        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
        tunnelInfoDto.setTunnelStatus( ERROR );
        tunnelInfoDto.setErrorLogs( errorLog );
        updateTunnelStatus( link, tunnelInfoDto, restClient );
    }


    static RestResult<Object> updateTunnelStatus( String link, TunnelInfoDto tunnelInfoDto, RestClient restClient )
    {
        try
        {
            byte[] body = DtoConverter.serialize( tunnelInfoDto );

            CommonDto commonDto = new CommonDto( body );

            return restClient.put( link, commonDto, Object.class );
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent tunnel peer data to hub.";
            LOG.error( mgs, e.getMessage() );
            return null;
        }
    }


    static TunnelInfoDto getPeerTunnelState( String link, RestClient restClient )
    {
        try
        {
            RestResult<TunnelInfoDto> restResult = restClient.get( link, TunnelInfoDto.class );


            LOG.debug( "Response: HTTP {} - {}", restResult.getStatus(), restResult.getReasonPhrase() );

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( "Error to get tunnel  data from Hub: HTTP {} - {}", restResult.getStatus(),
                        restResult.getError() );

                return null;
            }

            return restResult.getEntity();
        }
        catch ( Exception e )
        {
            sendError( link, e.getMessage(), restClient );
            LOG.error( e.getMessage() );
            return null;
        }
    }


    public static TunnelInfoDto parseResult( String result, TunnelInfoDto tunnelInfoDto )
    {
        result = result.replaceAll( "\n", "" );
        result = result.replaceAll( "\t", "_" );
        String[] ipport = result.split( "_" );
        result = ipport[0];
        ipport = result.split( ":" );

        tunnelInfoDto.setOpenedIp( String.valueOf( ipport[0] ) );
        tunnelInfoDto.setOpenedPort( String.valueOf( ipport[1] ) );
        return tunnelInfoDto;
    }


    public static void deleteAllTunnelsForIp( final Set<ResourceHost> resourceHosts, final String ip )
    {

        ResourceHost resourceHost = resourceHosts.iterator().next();

        CommandResult result = execute( resourceHost, String.format( GET_OPENED_TUNNELS_FOR_IP_COMMAND, ip ) );

        if ( result != null )
        {
            String[] data = result.getStdOut().split( "\n" );

            for ( String tunnel : data )
            {
                execute( resourceHost, String.format( DELETE_TUNNEL_COMMAND, tunnel ) );
            }
        }
    }
}
