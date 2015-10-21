package io.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.subutai.common.peer.RegistrationRequest;


/**
 * REST endpoint for registration process
 */
public interface RegistrationService
{
    @Path( "/register" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    RegistrationRequest processRegistrationRequest( RegistrationRequest registrationRequest );

    @Path( "/cancel" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void processCancelRequest( RegistrationRequest registrationRequest );

    @Path( "/reject" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void processRejectRequest( RegistrationRequest registrationRequest );

    @Path( "/approve" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    RegistrationRequest processApproveRequest( RegistrationRequest registrationRequest );

    @Path( "/unregister" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void processUnregisterRequest( RegistrationRequest registrationRequest );
}
