package io.subutai.core.bazaarmanager.api;


import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public abstract class BazaarRequester implements Runnable
{
    private static final AtomicInteger runningCount = new AtomicInteger( 0 );
    private static final Logger LOGGER = LoggerFactory.getLogger( BazaarRequester.class );

    protected final BazaarManager bazaarManager;

    protected final RestClient restClient;


    protected BazaarRequester( final BazaarManager bazaarManager, final RestClient restClient )
    {
        Preconditions.checkNotNull( bazaarManager );
        Preconditions.checkNotNull( restClient );

        this.bazaarManager = bazaarManager;
        this.restClient = restClient;
    }


    @Override
    public final void run()
    {
        if ( bazaarManager.canWorkWithBazaar() && !bazaarManager.isPeerUpdating() )
        {
            try
            {
                runningCount.incrementAndGet();

                request();
            }
            catch ( Exception e )
            {
                LOGGER.error( "Error in " + getClass().getName(), e );
            }
            finally
            {
                runningCount.decrementAndGet();
            }
        }
    }


    public static boolean areRequestorsRunning()
    {
        return runningCount.get() > 0;
    }


    public abstract void request() throws Exception;
}
