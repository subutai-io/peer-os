package io.subutai.common.exception.mapper;


import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


/**
 * Default server error exception mapper
 */
public class ServerExceptionMapper implements ExceptionMapper<ServerErrorException>
{
    @Override
    public Response toResponse( ServerErrorException e )
    {
        return Response.status( e.getResponse().getStatus() ).header( "exception", e.getMessage() )
                       .entity( "SERVER ERROR: " + e.getMessage() ).build();
    }
}