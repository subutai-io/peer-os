package io.subutai.core.executor.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    @POST
    @Path( "heartbeat" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response processHeartbeat( @FormParam( "heartbeat" ) String heartbeat );
}
