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
    public Response registerPublicKey( String message );


    @POST
    @Path( "requests/{id}/approve" )
    public Response approveRegistrationRequest( @PathParam( "id" ) String requestId );


    @POST
    @Path( "requests/{id}/reject" )
    public Response rejectRequest( @PathParam( "id" ) String requestId );


    @POST
    @Path( "requests/{id}/remove" )
    public Response removeRequest( @PathParam( "id" ) String requestId );


    @POST
    @Path( "verify/container-token" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response verifyContainerToken( String message );


    @GET
    @Path( "requests" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegistrationRequests();
}
