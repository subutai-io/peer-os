package io.subutai.core.kurjun.manager.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    //Register to HUB
    @GET
    @Path( "/authid" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getAuthId();

}
