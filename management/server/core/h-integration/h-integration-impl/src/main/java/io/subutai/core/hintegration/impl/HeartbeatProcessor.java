package io.subutai.core.hintegration.impl;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hearbeat processor
 */
public class HeartbeatProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartbeatProcessor.class );
    private List<CommandProcessor> processors = new CopyOnWriteArrayList<CommandProcessor>();


    public void addProcessor( CommandProcessor commandProcessor )
    {
        this.processors.add( commandProcessor );
    }


    public void addEndpoint() {

    }

    @Override
    public void run()
    {
        LOG.debug( "Hearbeat processor started..." );
        try
        {
            for ( CommandProcessor processor : this.processors )
            {
                // here the body of runner
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        LOG.debug( "Hearbeat processor done." );
    }
}
