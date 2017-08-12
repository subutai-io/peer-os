package io.subutai.core.registration.rest;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RegistrationRestService
{

    @POST
    @Path( "public-key" )
    Response registerPublicKey( String message );

    @POST
    @Path( "{rhId}/hostname/{hostname}" )
    Response changeRhHostname( @PathParam( "rhId" ) String rhId, @PathParam( "hostname" ) String hostname );


    @POST
    @Path( "requests/{id}/approve" )
    Response approveRegistrationRequest( @PathParam( "id" ) String requestId );


    @POST
    @Path( "requests/{id}/reject" )
    Response rejectRequest( @PathParam( "id" ) String requestId );


    @POST
    @Path( "requests/{id}/remove" )
    Response removeRequest( @PathParam( "id" ) String requestId );

    @POST
    @Path( "requests/{id}/unblock" )
    Response unblockRequest( @PathParam( "id" ) String requestId );


    @POST
    @Path( "verify/container-token" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response verifyContainerToken( String message );


    @GET
    @Path( "requests" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRegistrationRequests();
}
