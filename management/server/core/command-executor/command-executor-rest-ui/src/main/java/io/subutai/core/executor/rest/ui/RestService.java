package io.subutai.core.executor.rest.ui;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    Response executeCommand( @FormParam( "hostid" ) String hostId, @FormParam( "command" ) String content,
                             @FormParam( "environmentid" ) String environmentId, @FormParam( "path" ) String path,
                             @FormParam( "daemon" ) Boolean daemon, @FormParam( "timeout" ) Integer timeOut );

    @Path( "async" )
    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    Response executeCommandAsync( @FormParam( "hostid" ) String hostId, @FormParam( "command" ) String content,
                                  @FormParam( "environmentid" ) String environmentId, @FormParam( "path" ) String path,
                                  @FormParam( "daemon" ) Boolean daemon, @FormParam( "timeout" ) Integer timeOut );

    @Path( "async/{id}" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCommandResult( @PathParam( "id" ) final String id );
}