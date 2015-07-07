package io.subutai.core.test.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface TestRest
{
    @GET
    @Path( "{username}" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response auth( @PathParam( "username" ) String username );
}
