package org.safehaus.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.PeerInfo;


public interface RestService
{


    @POST
    @Path( "peer" )
    @Produces( { MediaType.APPLICATION_JSON } )
    @Consumes( MediaType.TEXT_PLAIN )
    public PeerInfo registerPeer( @QueryParam( "peer" ) String peer );

    @GET
    @Path( "id" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String getId();


    @POST
    @Path( "container/schedule" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response scheduleCloneContainers( @FormParam( "creatorPeerId" ) String creatorPeerId,
                                      @FormParam( "templates" ) String templates, @FormParam( "quantity" ) int quantity,
                                      @FormParam( "strategyId" ) String strategyId,
                                      @FormParam( "criteria" ) String criteria );

    @POST
    @Path( "container/destroy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyContainer( @FormParam( "hostId" ) String host );

    @POST
    @Path( "container/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startContainer( @FormParam( "hostId" ) String host );

    @POST
    @Path( "container/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopContainer( @FormParam( "hostId" ) String host );

    @POST
    @Path( "container/isconnected" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response isContainerConnected( @FormParam( "hostId" ) String hostId );


    @POST
    @Path( "template/get" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTemplate( @FormParam( "templateName" ) String templateName );

    @POST
    @Path( "environment/containers" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response environmentContainers( @FormParam( "environmentId" ) String envId );

    @GET
    @Path( "ping" )
    public Response ping();

    @POST
    @Path( "register" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response processRegisterRequest( @QueryParam( "peer" ) String peer );

    @DELETE
    @Path( "unregister" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response unregisterPeer( @QueryParam( "peerId" ) String peerId );

    @PUT
    @Path( "update" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response updatePeer( @QueryParam( "peer" ) String peer );


    @POST
    @Path( "container/quota" )
    Response setQuota( @FormParam( "hostId" ) String hostId, @FormParam( "quotaInfo" ) String quotaInfo );

    @GET
    @Path( "container/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getQuota( @QueryParam( "hostId" ) String hostId, @QueryParam( "quotaType" ) String quotaType );
}