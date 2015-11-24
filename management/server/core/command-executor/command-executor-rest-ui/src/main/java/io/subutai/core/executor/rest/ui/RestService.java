package io.subutai.core.executor.rest.ui;


import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    Response executeCommand( @FormParam( "hostId" ) String hostId,
                                    @FormParam( "command" ) String content,
                                    @FormParam( "path" ) String path );
}