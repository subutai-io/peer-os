package io.subutai.core.registration.rest;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RegistrationRestService
{
    @GET
    @Path( "public-key" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getPublicKey();

    @POST
    @Path( "public-key" )

    public Response registerPublicKey( String message );

    @POST
    @Path( "verify/container-token" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response verifyContainerToken( String message );
}
