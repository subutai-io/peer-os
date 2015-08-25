package io.subutai.core.registration.rest;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by talas on 8/25/15.
 */
public interface RegistrationRestService
{
    @GET
    @Path( "public-key" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKey();

    @POST
    @Path( "public-key" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response registerPublicKey( String message );
}
