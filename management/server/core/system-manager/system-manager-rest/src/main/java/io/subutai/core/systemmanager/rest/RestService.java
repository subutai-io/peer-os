package io.subutai.core.systemmanager.rest;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by ermek on 2/6/16.
 */
public interface RestService
{
    @GET
    @Path( "about" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSubutaiInfo();

    @GET
    @Path( "peer_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerSettings();

    @GET
    @Path( "kurjun_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getKurjunSettings();

    @GET
    @Path( "peer_policy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerPolicy();

    @GET
    @Path( "channel_settings" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getChannelSettings();

    @POST
    @Path( "update_peerowner" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response setPeerOwner();

    @GET
    @Path( "peerowner" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerOwner();
}
