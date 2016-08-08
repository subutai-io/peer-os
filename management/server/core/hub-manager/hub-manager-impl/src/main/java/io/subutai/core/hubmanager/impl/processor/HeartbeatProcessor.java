package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.HeartbeatResponseDto;


public class HeartbeatProcessor implements Runnable
{
    private static final int FAST_MODE_MAX = 30; // For 5 min

    private static final int BIG_INTERVAL_SECONDS = 60;

    public static final int SMALL_INTERVAL_SECONDS = 10;

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Set<StateLinkProcessor> processors = new HashSet<>();

    private final HubManagerImpl hubManager;

    private final HubRestClient restClient;

    private final String path;

    private long lastSentMillis = 0;

    private int fastModeLeft = 0;

    private volatile boolean inProgress = false;

    private String peerId;


    public HeartbeatProcessor( HubManagerImpl hubManager, HubRestClient restClient, String peerId )
    {
        this.hubManager = hubManager;
        this.restClient = restClient;
        this.peerId = peerId;

        path = String.format( "/rest/v1.3/peers/%s/heartbeat/", peerId );

        addShutDownHook();
    }


    private void addShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            public void run()
            {
                try
                {
                    log.warn( "Shutting down hub manager" );
                    //TODO send "i-am-off" heartbeat here
                }
                catch ( Exception e )
                {
                    //ignore
                }
            }
        } );
    }


    public HeartbeatProcessor addProcessor( StateLinkProcessor processor )
    {
        processors.add( processor );

        return this;
    }


    /**
     * Called by scheduler.
     */
    @Override
    public void run()
    {
        try
        {
            sendHeartbeat( false );
        }
        catch ( Exception e )
        {
            log.error( "Error to process heartbeat: ", e );
        }
    }


    /**
     * @param force is true if a heartbeat is sent not by scheduler, e.g. manually or triggered from Hub.
     *
     * Normally heartbeats happen with an interval defined by BIG_INTERVAL_SECONDS. But the "fast mode" option is used
     * to make heartbeats faster, i.e. in SMALL_INTERVAL_SECONDS. Return value of StateLinkProcessor sets this option.
     * See HubEnvironmentProcessor for example.
     */
    public void sendHeartbeat( boolean force ) throws Exception
    {
        if ( !hubManager.isRegistered() )
        {
            return;
        }

        long interval = ( System.currentTimeMillis() - lastSentMillis ) / 1000;

        boolean canSend = ( interval >= BIG_INTERVAL_SECONDS || force || fastModeLeft > 0 ) && !inProgress;

        if ( canSend )
        {
            log.info( "Sending heartbeat: interval={}, force={}, fastModeLeft={}", interval, force, fastModeLeft );

            doHeartbeat();
        }
    }


    private synchronized void doHeartbeat() throws Exception
    {
        log.info( "Heartbeat - START" );

        inProgress = true;

        lastSentMillis = System.currentTimeMillis();

        fastModeLeft--;

        try
        {
            String url = path + RandomStringUtils.randomNumeric( 7 );

            RestResult<HeartbeatResponseDto> restResult = restClient.post( url, null, HeartbeatResponseDto.class );

            if ( !restResult.isSuccess() )
            {
                if ( restResult.getStatus() == HttpStatus.SC_FORBIDDEN )
                {
                    hubManager.getConfigDataService().deleteConfig( peerId );
                }
                else
                {
                    throw new Exception( "Error to send heartbeat: " + restResult.getError() );
                }
            }

            HeartbeatResponseDto dto = restResult.getEntity();

            processStateLinks( dto.getStateLinks() );
        }
        catch ( Exception e )
        {
            throw new Exception( e.getMessage(), e );
        }
        finally
        {
            inProgress = false;
        }

        log.info( "Heartbeat - END" );
    }


    private void processStateLinks( Set<String> stateLinks )
    {
        log.info( "stateLinks: {}", stateLinks );

        for ( StateLinkProcessor processor : processors )
        {
            try
            {
                boolean fastModeAsked = processor.processStateLinks( stateLinks );

                if ( fastModeAsked )
                {
                    fastModeLeft = FAST_MODE_MAX;
                }
            }
            catch ( Exception e )
            {
                log.error( "Error to process state links: ", e );
            }
        }
    }
}
