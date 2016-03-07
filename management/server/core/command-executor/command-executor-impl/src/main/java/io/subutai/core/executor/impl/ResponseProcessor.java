package io.subutai.core.executor.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.command.Request;
import io.subutai.common.command.Response;


/**
 * Processes response
 */
public class ResponseProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( ResponseProcessor.class.getName() );

    private Response response;
    private CommandProcess process;
    private CommandProcessor processor;
    private Request request;


    public ResponseProcessor( final Response response, final CommandProcess process, final CommandProcessor processor,
                              final Request request )
    {
        Preconditions.checkNotNull( response );
        Preconditions.checkNotNull( process );
        Preconditions.checkNotNull( processor );

        this.response = response;
        this.process = process;
        this.processor = processor;
        this.request = request;
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
                processor.remove( request );
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
