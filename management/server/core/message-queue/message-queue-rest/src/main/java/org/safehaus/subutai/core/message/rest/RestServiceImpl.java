package org.safehaus.subutai.core.message.rest;


import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.message.api.MessageException;
import org.safehaus.subutai.core.message.api.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Messenger REST implementation
 */
public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private final MessageProcessor messageProcessor;


    public RestServiceImpl( final MessageProcessor messageProcessor )
    {
        Preconditions.checkNotNull( messageProcessor, "Message processor is null" );

        this.messageProcessor = messageProcessor;
    }


    @Override
    public Response processMessage( final String envelope )
    {
        try
        {
            messageProcessor.processMessage( envelope );
            return Response.accepted().build();
        }
        catch ( MessageException e )
        {
            LOG.error( "Error in processMessage", e );
            return Response.serverError().entity( e ).build();
        }
    }
}
