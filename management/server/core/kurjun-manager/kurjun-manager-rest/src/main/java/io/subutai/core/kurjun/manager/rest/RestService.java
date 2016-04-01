package io.subutai.core.kurjun.manager.rest;


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
    @Path( "urls" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getKurjunUrl();


    @POST
    @Path( "register" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response register( @FormParam( "id" ) String id );

    @POST
    @Path( "update" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response update( @FormParam( "id" ) String id, @FormParam( "url" ) String url  );

    @POST
    @Path( "signed-msg" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getSignedMessage( @FormParam( "signedMsg" ) String signedMsg, @FormParam( "id" ) String id );


    @POST
    @Path( "url/add" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response addUrl( @FormParam( "url" ) String url, @FormParam( "type" ) String type );
}
