package io.subutai.core.bazaarmanager.impl.processor;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.BazaarManagerImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.HeartbeatResponseDto;


public class HeartbeatProcessor implements Runnable
{
    private static final AtomicInteger runningCount = new AtomicInteger( 0 );

    private static final int FAST_MODE_MAX = 30; // For 5 min

    private static final int BIG_INTERVAL_SECONDS = 60;

    public static final int SMALL_INTERVAL_SECONDS = 10;

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Set<StateLinkProcessor> processors = new HashSet<>();

    private final BazaarManagerImpl bazaarManager;

    private final PeerManager peerManager;

    private final RestClient restClient;

    private final String path;

    private long lastSentMillis = 0;

    private volatile int fastModeLeft = 0;

    private volatile boolean inProgress = false;

    private String peerId;

    private ExecutorService processorPool = Executors.newFixedThreadPool( Common.MAX_EXECUTOR_SIZE );

    private volatile boolean isBazaarReachable = true;


    public HeartbeatProcessor( BazaarManagerImpl bazaarManager, PeerManager peerManager, RestClient restClient,
                               String peerId )
    {
        this.bazaarManager = bazaarManager;
        this.peerManager = peerManager;
        this.restClient = restClient;
        this.peerId = peerId;

        path = String.format( "/rest/v1/peers/%s/heartbeat/", peerId );

        addShutDownHook();
    }


    public boolean isBazaarReachable()
    {
        return isBazaarReachable;
    }


    private void addShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                notifyBazaarThatPeerIsOffline();
            }
        } );
    }


    public void notifyBazaarThatPeerIsOffline()
    {
        try
        {
            if ( !bazaarManager.isRegisteredWithBazaar() )
            {
                return;
            }

            log.warn( "Notifying Bazaar that peer is offline" );

            String url = path + "shutdown-hook";

            RestResult<Object> restResult = restClient.post( url, null );

            if ( restResult.isSuccess() )
            {
                log.info( "'Peer offline' notification successfully sent to Bazaar" );
            }
        }
        catch ( Exception e )
        {
            //ignore
        }
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
     * @param force is true if a heartbeat is sent not by scheduler, e.g. manually or triggered frombazaar.
     *
     * Normally heartbeats happen with an interval defined by BIG_INTERVAL_SECONDS. But the "fast mode" option is used
     * to make heartbeats faster, i.e. in SMALL_INTERVAL_SECONDS. Return value of StateLinkProcessor sets this option.
     */
    public void sendHeartbeat( boolean force ) throws BazaarManagerException
    {
        if ( !bazaarManager.isRegisteredWithBazaar() || bazaarManager.isPeerUpdating() )
        {
            return;
        }

        if ( !( peerManager.getLocalPeer().getState() == LocalPeer.State.READY && peerManager.getLocalPeer()
                                                                                             .isMHPresent() ) )
        {
            return;
        }

        long interval = ( System.currentTimeMillis() - lastSentMillis ) / 1000;

        boolean canSend = ( interval >= BIG_INTERVAL_SECONDS || force || fastModeLeft > 0 ) && !inProgress;

        if ( canSend )
        {
            log.info( "Sending heartbeat to Bazaar: interval={}, force={}, fastModeLeft={}", interval, force,
                    fastModeLeft );

            try
            {
                doHeartbeat();

                isBazaarReachable = true;
            }
            catch ( Exception e )
            {
                if ( RestClient.CONNECTION_EXCEPTION_MARKER.equals( e.getMessage() ) )
                {
                    isBazaarReachable = false;
                }

                throw e;
            }
        }
    }


    /**
     * Performs http request to Bazaar to check if the local peer is registered with it
     */
    private boolean isRegisteredWithBazaar()
    {
        RestResult<String> restResult =
                restClient.getPlain( String.format( "/rest/v1/peers/%s/register/check", peerId ), String.class );

        return restResult.getStatus() == HttpStatus.SC_OK;
    }


    private synchronized void doHeartbeat() throws BazaarManagerException
    {
        log.info( "Heartbeat - START" );

        inProgress = true;

        lastSentMillis = System.currentTimeMillis();

        fastModeLeft--;

        try
        {
            String url = path + RandomStringUtils.randomNumeric( 7 );

            RestResult<HeartbeatResponseDto> restResult = restClient.post( url, null, HeartbeatResponseDto.class );

            if ( RestClient.CONNECTION_EXCEPTION_MARKER.equals( restResult.getError() ) )
            {
                throw new IllegalStateException( RestClient.CONNECTION_EXCEPTION_MARKER );
            }

            if ( !restResult.isSuccess() )
            {
                if ( restResult.getStatus() == HttpStatus.SC_FORBIDDEN && !isRegisteredWithBazaar() )
                {
                    log.warn( "Local peer {} is not registered with Bazaar, deleting registration record from db",
                            peerId );

                    bazaarManager.getConfigDataService().deleteConfig( peerId );
                }
                else
                {
                    throw new BazaarManagerException( "Error to send heartbeat: " + restResult.getError() );
                }
            }

            HeartbeatResponseDto dto = restResult.getEntity();

            processStateLinks( dto.getStateLinks() );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e.getMessage(), e );
        }
        finally
        {
            inProgress = false;
        }

        log.info( "Heartbeat - END" );
    }


    public static boolean areProcessorsRunning()
    {
        return runningCount.get() > 0;
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
                        runningCount.incrementAndGet();

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
                    finally
                    {
                        runningCount.decrementAndGet();
                    }
                }
            } );
        }
    }
}
