package io.subutai.core.hubmanager.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    //Send heartbeat
    @POST
    @Path( "/send-heartbeat" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response sendHeartbeat();

    //Resend heartbeat
    @POST
    @Path( "/resend-heartbeat" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response triggerHeartbeat();

    //Register to HUB
    @POST
    @Path( "/register" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response register( @FormParam( "email" ) String email, @FormParam( "password" ) String password,
                       @FormParam( "peerName" ) String peerName, @FormParam( "peerScope" ) String peerScope );

    //Register to HUB
    @GET
    @Path( "/dns" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response getHubDns();

    //Unregister to HUB
    @DELETE
    @Path( "/unregister" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response unregister();

    //Check registration state
    @GET
    @Path( "/registration_state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRegistrationState();
}
