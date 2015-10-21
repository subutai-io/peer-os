package io.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.subutai.common.peer.RegistrationData;


/**
 * REST endpoint for registration process
 */
public interface RegistrationService
{
    @Path( "/register" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    RegistrationData processRegistrationRequest( RegistrationData registrationData );

    @Path( "/cancel" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void processCancelRequest( RegistrationData registrationData );

    @Path( "/reject" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void processRejectRequest( RegistrationData registrationData );

    @Path( "/approve" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    RegistrationData processApproveRequest( RegistrationData registrationData );

    @Path( "/unregister" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void processUnregisterRequest( RegistrationData registrationData );
}
