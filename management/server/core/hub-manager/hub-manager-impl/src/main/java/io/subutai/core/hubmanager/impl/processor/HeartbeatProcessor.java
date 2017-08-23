package io.subutai.core.hubmanager.impl.processor;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;

import io.subutai.common.settings.Common;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
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

    private volatile int fastModeLeft = 0;

    private volatile boolean inProgress = false;

    private String peerId;

    private ExecutorService processorPool = Executors.newFixedThreadPool( Common.MAX_EXECUTOR_SIZE );

    private volatile boolean isHubReachable = true;


    public HeartbeatProcessor( HubManagerImpl hubManager, HubRestClient restClient, String peerId )
    {
        this.hubManager = hubManager;
        this.restClient = restClient;
        this.peerId = peerId;

        path = String.format( "/rest/v1/peers/%s/heartbeat/", peerId );

        addShutDownHook();
    }


    public boolean isHubReachable()
    {
        return isHubReachable;
    }


    private void addShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    log.warn( "Shutting down hub manager" );
                    String url = path + "shutdown-hook";

                    RestResult<Object> restResult = restClient.post( url, null );

                    if ( restResult.isSuccess() )
                    {
                        log.info( "Shutdown hook successfully sent to Hub" );
                    }
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
            log.error( "Error performing heartbeat: " + e.getMessage() );
        }
    }


    /**
     * @param force is true if a heartbeat is sent not by scheduler, e.g. manually or triggered from Hub.
     *
     * Normally heartbeats happen with an interval defined by BIG_INTERVAL_SECONDS. But the "fast mode" option is used
     * to make heartbeats faster, i.e. in SMALL_INTERVAL_SECONDS. Return value of StateLinkProcessor sets this option.
     * See HubEnvironmentProcessor for example.
     */
    public void sendHeartbeat( boolean force ) throws HubManagerException
    {
        if ( !hubManager.isRegisteredWithHub() )
        {
            return;
        }

        long interval = ( System.currentTimeMillis() - lastSentMillis ) / 1000;

        boolean canSend = ( interval >= BIG_INTERVAL_SECONDS || force || fastModeLeft > 0 ) && !inProgress;

        if ( canSend )
        {
            log.info( "Sending heartbeat to HUB: interval={}, force={}, fastModeLeft={}", interval, force,
                    fastModeLeft );

            try
            {
                doHeartbeat();

                isHubReachable = true;
            }
            catch ( Exception e )
            {
                if ( HubRestClient.CONNECTION_EXCEPTION_MARKER.equals( e.getMessage() ) )
                {
                    isHubReachable = false;
                }

                throw e;
            }
        }
    }


    private synchronized void doHeartbeat() throws HubManagerException
    {
        log.info( "Heartbeat - START" );

        inProgress = true;

        lastSentMillis = System.currentTimeMillis();

        fastModeLeft--;

        try
        {
            String url = path + RandomStringUtils.randomNumeric( 7 );

            RestResult<HeartbeatResponseDto> restResult = restClient.post( url, null, HeartbeatResponseDto.class );

            if ( HubRestClient.CONNECTION_EXCEPTION_MARKER.equals( restResult.getError() ) )
            {
                throw new IllegalStateException( HubRestClient.CONNECTION_EXCEPTION_MARKER );
            }

            if ( !restResult.isSuccess() )
            {
                //TODO here we need to use a dedicated http code for letting know that peer is not registered with Hub
                //otherwise it gets unregistered for any auth error
                if ( restResult.getStatus() == HttpStatus.SC_FORBIDDEN )
                {
                    hubManager.getConfigDataService().deleteConfig( peerId );
                }
                else
                {
                    throw new HubManagerException( "Error to send heartbeat: " + restResult.getError() );
                }
            }

            HeartbeatResponseDto dto = restResult.getEntity();

            processStateLinks( dto.getStateLinks() );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e.getMessage(), e );
        }
        finally
        {
            inProgress = false;
        }

        log.info( "Heartbeat - END" );
    }


    private void processStateLinks( final Set<String> stateLinks )
    {
        log.info( "stateLinks: {}", stateLinks );

        final Set<String> unmodifiableSet = Collections.unmodifiableSet( stateLinks );

        for ( final StateLinkProcessor processor : processors )
        {
            processorPool.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        boolean fastModeAsked = processor.processStateLinks( unmodifiableSet );

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
            } );
        }
    }
}
