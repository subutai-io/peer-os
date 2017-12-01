package io.subutai.core.environment.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * REST endpoint for environment manager
 */
public interface RestService
{
    @Path( "/" )
    @POST
    Response createEnvironment( @FormParam( "topology" ) String topology );

    @Path( "/{environmentId}" )
    @PUT
    Response growEnvironment( @PathParam( "environmentId" ) String environmentId,
                              @FormParam( "topology" ) String topology );

    @Path( "/" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    Response listEnvironments();


    @Path( "/{environmentId}" )
    @DELETE
    Response destroyEnvironment( @PathParam( "environmentId" ) String environmentId );


    @DELETE
    @Path( "containers/{containerId}" )
    Response destroyContainer( @PathParam( "containerId" ) String containerId );
    //----------------


    @Deprecated
    @Path( "/{containerIp}/info" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response placeEnvironmentInfoByContainerIp( @PathParam( "containerIp" ) String containerIp );
}
