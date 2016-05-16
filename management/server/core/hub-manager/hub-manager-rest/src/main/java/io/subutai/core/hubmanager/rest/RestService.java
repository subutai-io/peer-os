package io.subutai.core.hubmanager.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    //Send heartbeat
    @POST
    @Path( "/send-heartbeat" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response sendHeartbeat( @QueryParam( "hubIp" ) String hubIp );

    //Resend heartbeat
    @POST
    @Path( "/resend-heartbeat" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response triggerHeartbeat();

    //Register to HUB
    @POST
    @Path( "/register" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response register( @FormParam( "hubIp" ) String hubIp, @FormParam( "email" ) String email,
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
    public Response unregister();

    //Check registration state
    @GET
    @Path( "/registration_state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegistrationState();

    //TODO after finish ENVIRONMENT MANAGEMENT should delete this method
    @POST
    @Path( "/upSite" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response upSite();

    //TODO after finish ENVIRONMENT MANAGEMENT should delete this method
    @DELETE
    @Path( "/downSite" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response downSite();

    //TODO after finish ENVIRONMENT MANAGEMENT should delete this method
    @GET
    @Path( "/checksum" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response checksum();
}
