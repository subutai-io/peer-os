package io.subutai.common.exception.mapper;


import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


/**
 * Default client  exception mapper
 */
public class ClientExceptionMapper implements ExceptionMapper<ClientErrorException>
{
    @Override
    public Response toResponse( ClientErrorException e )
    {
        return Response.status( e.getResponse().getStatus() ).header( "exception", e.getMessage() )
                       .entity( "CLIENT ERROR: " + e.getMessage() ).build();
    }
}
