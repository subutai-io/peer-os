package io.subutai.core.environment.metadata.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.hub.share.event.EventMessage;


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
    Response echo( @HeaderParam( "containerId" ) String containerId, @PathParam( "message" ) String message );

    @Path( "/info" )
    @Produces( MediaType.APPLICATION_JSON )
    @GET
    Response getEnvironmentDto( @HeaderParam( "environmentId" ) String environmentId );

    @Path( "/event" )
    @Consumes( MediaType.APPLICATION_JSON )
    @POST
    Response pushEvent( @HeaderParam( "subutaiOrigin" ) String origin, EventMessage event );
}
