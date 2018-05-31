package io.subutai.core.template.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listTemplates();

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    @Path( "own" )
    Response listOwnTemplates();

    @GET
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "fingerprint" )
    Response getFingerprint();

    @GET
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "token" )
    Response getObtainedCdnToken();

    @POST
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "token" )
    Response obtainCdnToken( @FormParam( "signedFingerprint" ) String signedFingerprint );

    @GET
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "registered" )
    Response isRegisteredWithCdn();
}
