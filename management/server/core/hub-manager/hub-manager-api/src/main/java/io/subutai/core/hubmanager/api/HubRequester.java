package io.subutai.core.hubmanager.api;


import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public abstract class HubRequester implements Runnable
{
    private static final AtomicInteger runningCount = new AtomicInteger( 0 );
    private static final Logger LOGGER = LoggerFactory.getLogger( HubRequester.class );

    protected final HubManager hubManager;

    protected final RestClient restClient;


    protected HubRequester( final HubManager hubManager, final RestClient restClient )
    {
        Preconditions.checkNotNull( hubManager );
        Preconditions.checkNotNull( restClient );

        this.hubManager = hubManager;
        this.restClient = restClient;
    }


    @Override
    public final void run()
    {
        if ( hubManager.canWorkWithHub() && !hubManager.isPeerUpdating() )
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
