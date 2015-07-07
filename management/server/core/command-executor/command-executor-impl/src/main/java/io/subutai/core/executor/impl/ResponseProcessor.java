package io.subutai.core.executor.impl;


import io.subutai.common.command.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Processes response
 */
public class ResponseProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( ResponseProcessor.class.getName() );

    private Response response;
    private CommandProcess process;
    private CommandProcessor processor;


    public ResponseProcessor( final Response response, final CommandProcess process, final CommandProcessor processor )
    {
        Preconditions.checkNotNull( response );
        Preconditions.checkNotNull( process );
        Preconditions.checkNotNull( processor );

        this.response = response;
        this.process = process;
        this.processor = processor;
    }


    @Override
    public void run()
    {
        process.appendResponse( response );

        try
        {
            process.getCallback().onResponse( response, process.getResult() );

            if ( process.isDone() )
            {
                //remove process from command processor
                processor.remove( response.getCommandId() );
                //stop process
                process.stop();
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error notifying callback", e );
        }
    }
}
