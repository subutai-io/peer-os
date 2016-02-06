package io.subutai.core.systemmanager.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by ermek on 2/5/16.
 */
public interface RestService
{
    @GET
    @Path( "subutai/about" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getSubutaiInfo();

}
