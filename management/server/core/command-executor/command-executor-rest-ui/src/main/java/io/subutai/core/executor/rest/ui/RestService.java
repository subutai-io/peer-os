package io.subutai.core.executor.rest.ui;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response executeCommand( @FormParam( "hostId" ) String hostId,
                                   @FormParam( "command" ) String content );

    @GET
    @Path( "resource_hosts" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getResourceHosts( );
}