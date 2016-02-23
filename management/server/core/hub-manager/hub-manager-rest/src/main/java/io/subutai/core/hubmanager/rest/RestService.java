package io.subutai.core.hubmanager.rest;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    //Send heartbeat
    @POST
    @Path( "/send-heartbeat" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response sendHeartbeat( @QueryParam( "hubIp" ) String hubIp );

    //Register to HUB
    @POST
    @Path( "/register" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response register(@FormParam ( "hubIp" ) String hubIp, @FormParam( "email" ) String email,
							 @FormParam( "password" ) String password );

    //Send resource host configurations
    @POST
    @Path( "/send-rh-configurations" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response sendRHConfigurations( @QueryParam( "hubIp" ) String hubIp );

    //Register to HUB
    @GET
    @Path( "/dns" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getHubDns();

    //Unregister to HUB
    @DELETE
    @Path( "/unregister" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response unregister( );
}
