package io.subutai.common.exception.mapper;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


/**
 * Default throwable mapper
 */
public class ThrowableMapper implements ExceptionMapper<Throwable>
{
    @Override
    public Response toResponse( Throwable e )
    {
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).header( "exception", e.getMessage() )
                       .entity( "DATA ERROR: " + e.getMessage() ).build();
    }
}
