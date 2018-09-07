package io.subutai.core.bazaarmanager.impl.requestor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.core.appender.SubutaiErrorEvent;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.impl.ConfigManager;
import io.subutai.core.bazaarmanager.impl.BazaarManagerImpl;
import io.subutai.core.bazaarmanager.impl.LogListenerImpl;
import io.subutai.bazaar.share.dto.SubutaiSystemLog;
import io.subutai.bazaar.share.dto.SystemLogsDto;


public class LogProcessor extends BazaarRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private ConfigManager configManager;


    private LogListenerImpl logListener;


    public LogProcessor( final ConfigManager configManager, final BazaarManagerImpl bazaarManager,
                         final LogListenerImpl logListener, final RestClient restClient )
    {
        super( bazaarManager, restClient );
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

                log.debug( "Sending System logs to Bazaar:" );

                RestResult<Object> restResult = restClient.post( "/rest/v1/system-bugs", logsDto );

                if ( restResult.getStatus() != HttpStatus.SC_NO_CONTENT )
                {
                    log.warn( "Could not send logs to Bazaar {}", restResult.getError() );
                }
                else
                {

                    log.debug( "System logs sent to Bazaar successfully." );
                }
            }
            catch ( Exception e )
            {
                log.warn( "Could not send logs to Bazaar {}", e.getMessage() );
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
