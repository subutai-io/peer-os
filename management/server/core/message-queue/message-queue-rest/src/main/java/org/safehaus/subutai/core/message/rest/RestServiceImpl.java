package org.safehaus.subutai.core.message.rest;


import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.message.api.MessageException;
import org.safehaus.subutai.core.message.api.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Queue Rest implementation
 */
public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private final Queue queue;


    public RestServiceImpl( final Queue queue )
    {
        Preconditions.checkNotNull( queue, "Queue is null" );

        this.queue = queue;
    }


    @Override
    public Response processMessage( final String message )
    {
        try
        {
            queue.processMessage( message );
            return Response.accepted().build();
        }
        catch ( MessageException e )
        {
            LOG.error( "Error in processMessage", e );
            return Response.serverError().entity( e ).build();
        }
    }
}
