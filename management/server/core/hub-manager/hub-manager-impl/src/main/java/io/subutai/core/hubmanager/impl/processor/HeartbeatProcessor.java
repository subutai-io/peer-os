package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.HeartbeatResponseDto;


public class HeartbeatProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Set<StateLinkProcessor> processors = new HashSet<>();

    private final HubManagerImpl hubManager;

    private final String path;

    private final HubRestClient restClient;


    public HeartbeatProcessor( HubManagerImpl hubManager, ConfigManager configManager )
    {
        this.hubManager = hubManager;

        path = String.format( "/rest/v1.2/peers/%s/heartbeat", configManager.getPeerId() );

        restClient = new HubRestClient( configManager );
    }


    public HeartbeatProcessor addProcessor( StateLinkProcessor processor )
    {
        processors.add( processor );

        return this;
    }


    @Override
    public void run()
    {
        try
        {
            sendHeartbeat();
        }
        catch ( HubPluginException e )
        {
            log.error( "Error to process heartbeat: ", e );
        }
    }


    public void sendHeartbeat() throws HubPluginException
    {
        if ( !hubManager.isRegistered() )
        {
            return;
        }

        try
        {
            RestResult<HeartbeatResponseDto> restResult = restClient.put( path, null, HeartbeatResponseDto.class );

            if ( !restResult.isSuccess() )
            {
                throw new HubPluginException( restResult.getError() );
            }

            HeartbeatResponseDto dto = restResult.getEntity();

            processStateLinks( dto.getStateLinks() );
        }
        catch ( Exception e )
        {
            throw new HubPluginException( e.getMessage(), e );
        }
    }


    private void processStateLinks( Set<String> stateLinks )
    {
        log.info( "stateLinks: {}", stateLinks );

        for ( StateLinkProcessor processor : processors )
        {
            try
            {
                processor.processStateLinks( stateLinks );
            }
            catch ( HubPluginException e )
            {
                log.error( "Error to process state links: ", e );
            }
        }
    }
}
