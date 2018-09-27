package io.subutai.core.bazaarmanager.impl.tunnel;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.bazaar.share.common.util.DtoConverter;
import io.subutai.bazaar.share.dto.CommonDto;
import io.subutai.bazaar.share.dto.TunnelInfoDto;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;

import static io.subutai.bazaar.share.dto.TunnelInfoDto.TunnelStatus.ERROR;


public class TunnelHelper
{
    private static final Logger LOG = LoggerFactory.getLogger( TunnelHelper.class );


    private static final String DELETE_TUNNEL_COMMAND = "subutai tunnel del %s";
    private static final String GET_OPENED_TUNNELS_FOR_IP_COMMAND = "subutai tunnel list | grep %s | awk '{print $2}'";


    private TunnelHelper()
    {
        throw new IllegalAccessError( "Utility class" );
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
            String mgs = "Could not sent tunnel peer data to Bazaar.";
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
                LOG.error( "Error to get tunnel  data from Bazaar: HTTP {} - {}", restResult.getStatus(),
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

        for ( ResourceHost resourceHost : resourceHosts )
        {
            try
            {
                CommandResult result = resourceHost
                        .execute( new RequestBuilder( String.format( GET_OPENED_TUNNELS_FOR_IP_COMMAND, ip ) ) );

                if ( result.hasSucceeded() )
                {
                    String[] data = result.getStdOut().split( "\n" );

                    for ( String tunnel : data )
                    {
                        if ( !tunnel.isEmpty() )
                        {
                            resourceHost
                                    .execute( new RequestBuilder( String.format( DELETE_TUNNEL_COMMAND, tunnel ) ) );
                        }
                    }
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage() );
            }
        }
    }
}
