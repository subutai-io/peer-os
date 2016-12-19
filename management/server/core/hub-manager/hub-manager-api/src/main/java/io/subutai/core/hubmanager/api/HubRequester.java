package io.subutai.core.hubmanager.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public abstract class HubRequester implements Runnable
{
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
        if ( hubManager.canWorkWithHub() )
        {
            try
            {
                request();
            }
            catch ( Exception e )
            {
                LOGGER.error( "Error in " + getClass().getName(), e );
            }
        }
    }


    public abstract void request() throws Exception;
}
