package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.appender.SubutaiErrorEvent;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.LogListenerImpl;
import io.subutai.hub.share.dto.SubutaiSystemLog;
import io.subutai.hub.share.dto.SystemLogsDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class HubLoggerProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private ConfigManager configManager;

    private HubManagerImpl hubManager;

    private LogListenerImpl logListener;


    public HubLoggerProcessor( final ConfigManager configManager, final HubManagerImpl hubManager,
                               LogListenerImpl logListener )
    {
        this.configManager = configManager;
        this.hubManager = hubManager;
        this.logListener = logListener;
    }


    @Override
    public void run()
    {
        Set<SubutaiErrorEvent> subutaiErrorEvents = logListener.getSubutaiErrorEvents();

        if ( !subutaiErrorEvents.isEmpty() && hubManager.isRegistered() && hubManager.isHubReachable() )
        {
            WebClient client = null;
            try
            {
                client = configManager.getTrustedWebClientWithAuth( "/rest/v1/system-bugs", configManager.getHubIp() );

                SystemLogsDto logsDto = new SystemLogsDto();
                logsDto.setSubutaiSystemLogs( toSubutaiSystemLogs( subutaiErrorEvents ) );

                byte[] plainData = JsonUtil.toCbor( logsDto );
                byte[] encryptedData = configManager.getMessenger().produce( plainData );

                log.debug( "Sending System logs to HUB:" );

                Response r = client.post( encryptedData );

                if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
                {
                    log.warn( "Could not send logs to Hub {}", r.readEntity( String.class ) );
                }
                else
                {

                    log.debug( "System logs sent to HUB successfully." );
                }

                r.close();
            }
            catch ( Exception e )
            {
                log.warn( "Could not send logs to Hub {}", e.getMessage() );
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


    private Set<SubutaiSystemLog> toSubutaiSystemLogs( final Set<SubutaiErrorEvent> subutaiErrorEvents )
    {
        final Set<SubutaiSystemLog> result = new HashSet<>();
        for ( SubutaiErrorEvent e : subutaiErrorEvents )
        {
            result.add(
                    new SubutaiSystemLog( "PEER", SubutaiSystemLog.LogType.ERROR, e.getTimeStamp(), e.getLoggerName(),
                            e.getRenderedMessage(), e.getStackTrace() ) );
        }
        return result;
    }
}
