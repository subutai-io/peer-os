package io.subutai.core.registration.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;


/**
 * Created by talas on 8/25/15.
 */
public interface RestService
{
    @GET
    @Path( "public-key" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPublicKey();

    @POST
    @Path( "public-key" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response registerPublicKey( @FormParam( "Message" ) String message );
}
