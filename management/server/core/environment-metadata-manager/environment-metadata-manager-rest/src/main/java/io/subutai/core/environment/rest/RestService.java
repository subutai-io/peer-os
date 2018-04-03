package io.subutai.core.environment.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


/**
 * REST endpoint for environment manager
 */
public interface RestService
{
    @Path( "/issue/{containerIp}" )
    @GET
    Response issueToken( @PathParam( "containerIp" ) String containerIp );

}
