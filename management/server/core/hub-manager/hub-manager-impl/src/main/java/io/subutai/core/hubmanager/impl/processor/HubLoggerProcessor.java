package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.core.appender.SubutaiErrorEvent;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.LogListenerImpl;
import io.subutai.hub.share.dto.SubutaiSystemLog;
import io.subutai.hub.share.dto.SystemLogsDto;


public class HubLoggerProcessor extends HubRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private ConfigManager configManager;


    private LogListenerImpl logListener;


    public HubLoggerProcessor( final ConfigManager configManager, final HubManagerImpl hubManager,
                               final LogListenerImpl logListener, final RestClient restClient )
    {
        super( hubManager, restClient );
        this.configManager = configManager;
        this.logListener = logListener;
    }


    @Override
    public void request()
    {
        Set<SubutaiErrorEvent> subutaiErrorEvents = logListener.getSubutaiErrorEvents();

        if ( !subutaiErrorEvents.isEmpty() )
        {
            try
            {
                SystemLogsDto logsDto = new SystemLogsDto();

                logsDto.setSubutaiSystemLogs( toSubutaiSystemLogs( subutaiErrorEvents ) );

                log.debug( "Sending System logs to HUB:" );

                RestResult<Object> restResult = restClient.post( "/rest/v1/system-bugs", logsDto );

                if ( restResult.getStatus() != HttpStatus.SC_NO_CONTENT )
                {
                    log.warn( "Could not send logs to Hub {}", restResult.getError() );
                }
                else
                {

                    log.debug( "System logs sent to HUB successfully." );
                }
            }
            catch ( Exception e )
            {
                log.warn( "Could not send logs to Hub {}", e.getMessage() );
            }
        }
    }


    private Set<SubutaiSystemLog> toSubutaiSystemLogs( final Set<SubutaiErrorEvent> subutaiErrorEvents )
    {
        final Set<SubutaiSystemLog> result = new HashSet<>();
        for ( SubutaiErrorEvent e : subutaiErrorEvents )
        {
            result.add( new SubutaiSystemLog( SubutaiSystemLog.LogSource.PEER, configManager.getPeerId(),
                    SubutaiSystemLog.LogType.ERROR, e.getTimeStamp(), e.getLoggerName(), e.getRenderedMessage(),
                    e.getStackTrace() ) );
        }
        return result;
    }
}
