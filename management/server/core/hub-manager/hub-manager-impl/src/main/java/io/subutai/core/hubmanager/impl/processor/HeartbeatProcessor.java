package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.hub.share.dto.HeartbeatResponseDto;
import io.subutai.hub.share.json.JsonUtil;


public class HeartbeatProcessor implements Runnable
{
    private static final String URL = "/rest/v1.2/peers/%s/heartbeat";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Set<StateLinkProcessor> processors = new HashSet<>();

    private ConfigManager configManager;

    private IntegrationImpl manager;


    public HeartbeatProcessor( IntegrationImpl integration, ConfigManager configManager )
    {
        this.manager = integration;
        this.configManager = configManager;
    }


    public void addProcessor( StateLinkProcessor processor )
    {
        processors.add( processor );
    }


    @Override
    public void run()
    {
        sendHeartbeat();
    }


    public void sendHeartbeat()
    {
        if ( !manager.getRegistrationState() )
        {
            return;
        }

        try
        {
            String path = String.format( URL, configManager.getPeerId() );

            WebClient webClient = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response response = webClient.put( null );

            log.debug( "res.status: {}", response.getStatus() );

            if ( response.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error response for heartbeat: ", response.readEntity( String.class ) );
                return;
            }

            byte[] data = configManager.readContent( response );

            if ( data == null )
            {
                log.error( "Empty response for heartbeat" );
                return;
            }

            HeartbeatResponseDto dto = JsonUtil.fromCbor( configManager.getMessenger().consume( data ), HeartbeatResponseDto.class );

            log.debug( "State links: {}", dto.getStateLinks() );

            for ( StateLinkProcessor processor : processors )
            {
                try
                {
                    processor.processStateLinks( dto.getStateLinks() );
                }
                catch ( HubPluginException e )
                {
                    log.error( e.getMessage() );
                }
            }

            response.close();
            webClient.close();
        }
        catch ( Exception e )
        {
            log.error( "Error to send heartbeat: ", e );
        }
    }
}
