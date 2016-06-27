package io.subutai.core.hubmanager.impl.tunnel;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;

import static io.subutai.hub.share.dto.TunnelInfoDto.TunnelStatus.ERROR;


public class TunnelHelper
{
    private static final Logger LOG = LoggerFactory.getLogger( TunnelHelper.class );

    private static String COMMAND = "";


    public static CommandResult execute( ResourceHost resourceHost, String cmd )
    {
        COMMAND = cmd;
        boolean exec = true;
        int tryCount = 0;

        while ( exec )
        {
            tryCount++;
            exec = tryCount > 3 ? false : true;
            try
            {
                CommandResult result = resourceHost.execute( new RequestBuilder( cmd ) );

                if ( result.getExitCode() == 0 )
                {
                    exec = false;
                }

                return result;
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage() );
                e.printStackTrace();
            }

            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static void sendError( String link, String errorLog, ConfigManager configManager )
    {
        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
        tunnelInfoDto.setTunnelStatus( ERROR );
        tunnelInfoDto.setErrorLogs( errorLog );
        updateTunnelStatus( link, tunnelInfoDto, configManager );
    }


    public static Response updateTunnelStatus( String link, TunnelInfoDto tunnelInfoDto, ConfigManager configManager )
    {
        WebClient client = null;
        try
        {
            client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            byte[] cborData = JsonUtil.toCbor( tunnelInfoDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            return client.put( encryptedData );
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent tunnel peer data to hub.";
            LOG.error( mgs, e.getMessage() );
            return null;
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }


    public static TunnelInfoDto getPeerTunnelState( String link, ConfigManager configManager )
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
            sendError( link, e.getMessage(), configManager );
            LOG.error( e.getMessage() );
            return null;
        }
    }


    public static TunnelInfoDto parseResult( String link, String result, ConfigManager configManager )
    {
        TunnelInfoDto tunnelInfoDto = new TunnelInfoDto();
        String[] data = result.split( ":" );

        try
        {
            tunnelInfoDto.setOpenedIp( data[0] );
            if ( data[1].contains( " " ) )
            {
                tunnelInfoDto.setOpenedPort( data[1].split( " " )[0] );
            }
            else
            {
                tunnelInfoDto.setOpenedPort( data[1] );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            sendError( link, "Executed: " + COMMAND + "   output: " + result, configManager );
            return null;
        }
        return tunnelInfoDto;
    }
}
