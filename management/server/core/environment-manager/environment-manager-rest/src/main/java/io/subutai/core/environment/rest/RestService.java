package io.subutai.core.environment.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;


/**
 * REST endpoint for environment manager
 */
public interface RestService
{
    @Path( "/" )
    @POST
    Response createEnvironment( @FormParam( "topology" ) String topology ) throws EnvironmentCreationException;

    @Path( "/{environmentId}" )
    @PUT
    Response growEnvironment( @PathParam( "environmentId" ) String environmentId,
                              @FormParam( "topology" ) String topology )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    @Path( "/" )
    @GET
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response listEnvironments();

    @Path( "/{environmentId}" )
    @GET
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getEnvironment( @PathParam( "environmentId" ) String environmentId );

    @Path( "/{containerIp}/info" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response placeEnvironmentInfoByContainerIp( @PathParam( "containerIp" ) String containerIp );
}
