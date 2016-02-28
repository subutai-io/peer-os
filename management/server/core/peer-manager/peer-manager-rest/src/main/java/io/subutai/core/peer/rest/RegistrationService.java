package io.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.peer.RegistrationData;


/**
 * REST endpoint for registration process
 */
public interface RegistrationService
{
    @Path( "/info" )
    @GET
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getPeerInfo();

    @Path( "/register" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response processRegistrationRequest( RegistrationData registrationData );

    @Path( "/cancel" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response processCancelRequest( RegistrationData registrationData );

    @Path( "/reject" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response processRejectRequest( RegistrationData registrationData );

    @Path( "/approve" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response processApproveRequest( RegistrationData registrationData );

    @Path( "/unregister" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response processUnregisterRequest( RegistrationData registrationData );
}
