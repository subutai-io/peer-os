package io.subutai.core.environment.metadata.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


/**
 * REST endpoint for environment metadata manager
 */
public interface RestService
{

    @Path( "/token/{containerIp}" )
    @GET
    Response issueToken( @PathParam( "containerIp" ) String containerIp );

    @Path( "/echo/{message}" )
    @GET
    Response echo( @PathParam( "message" ) String message );
}
