package io.subutai.core.hubmanager.impl.tunnel;


import java.util.Set;

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
import io.subutai.hub.share.dto.SystemLogsDto;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.json.JsonUtil;


public class TunnelHelper
{
    private static final Logger LOG = LoggerFactory.getLogger( TunnelHelper.class );


    public static CommandResult execute( ResourceHost resourceHost, String cmd )
    {
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


    public static void sendLogs( Set<String> logs, ConfigManager configManager )
    {
        WebClient client = null;
        try
        {
            client = configManager.getTrustedWebClientWithAuth( "/rest/v1/system-bugs", configManager.getHubIp() );

            SystemLogsDto logsDto = new SystemLogsDto();
            logsDto.setLogs( logs );

            byte[] plainData = JsonUtil.toCbor( logsDto );
            byte[] encryptedData = configManager.getMessenger().produce( plainData );

            LOG.debug( "Sending System logs to HUB:" );

            Response response = client.post( encryptedData );

            if ( response.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                LOG.warn( "Could not send logs to Hub {}", response.readEntity( String.class ) );
            }
            else
            {

                LOG.debug( "System logs sent to HUB successfully." );
            }

            response.close();
        }
        catch ( Exception e )
        {
            LOG.warn( "Could not send logs to Hub {}", e.getMessage() );
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
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
}
