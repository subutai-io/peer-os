package io.subutai.core.identity.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @POST
    @Path( "gettoken" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response createTokenPOST( @FormParam( "username" ) String userName, @FormParam( "password" ) String password );

    @GET
    @Path( "gettoken" )
    @Produces( MediaType.TEXT_PLAIN )
    Response createTokenGET( @QueryParam( "username" ) String userName, @QueryParam( "password" ) String password );

    @GET
    @Path( "signtoken" )
    @Produces( MediaType.TEXT_PLAIN )
    Response getSignToken();

    @POST
    @Path( "auth" )
    @Produces( MediaType.APPLICATION_JSON )
    Response authenticate( @FormParam( "type" ) int type, @FormParam( "username" ) String userName,
                           @FormParam( "password" ) String password );

    //TODO>>> Seems these are obsolete.  check and remove:
    @PUT
    @Path( "authid" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response updateAuthId( @FormParam( "type" ) int type, @FormParam( "username" ) String userName,
                                  @FormParam( "password" ) String password, @FormParam( "authid" ) String authId );

    @POST
    @Path( "authid" )
    @Produces( MediaType.TEXT_PLAIN )
    Response getAuthId( @FormParam( "type" ) int type, @FormParam( "username" ) String userName,
                        @FormParam( "password" ) String password );

    //<<<TODO
}