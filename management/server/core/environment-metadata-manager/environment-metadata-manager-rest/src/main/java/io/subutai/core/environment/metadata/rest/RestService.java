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

import io.subutai.bazaar.share.event.payload.Payload;
import io.subutai.common.host.SubutaiOrigin;
import io.subutai.core.identity.api.IdentityManager;


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

    @Path( "/info/{type}" )
    @Produces( MediaType.APPLICATION_JSON )
    @GET
    Response getEnvironmentDto( @HeaderParam( IdentityManager.SUBUTAI_ORIGIN_HEADER_KEY ) SubutaiOrigin origin, @PathParam( "type" ) String type );

    @Path( "/event" )
    @Consumes( MediaType.APPLICATION_JSON )
    @POST
    Response pushEvent( @HeaderParam( IdentityManager.SUBUTAI_ORIGIN_HEADER_KEY ) SubutaiOrigin origin,
                        Payload payload );
}
