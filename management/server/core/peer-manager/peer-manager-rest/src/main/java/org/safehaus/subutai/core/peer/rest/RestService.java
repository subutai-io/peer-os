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

import org.safehaus.subutai.common.peer.PeerInfo;


public interface RestService
{


    @Deprecated
    @POST
    @Path( "peer" )
    @Produces( { MediaType.APPLICATION_JSON } )
    @Consumes( MediaType.TEXT_PLAIN )
    public PeerInfo registerPeer( @QueryParam( "peer" ) String peer );

    @GET
    @Path( "id" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String getId();

    @GET
    @Path( "registered_peers" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegisteredPeers();


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

    @GET
    @Path( "container/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );

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


    //*********** Quota functions ***************

    @GET
    @Path( "container/resource/usage" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getProcessResourceUsage( @QueryParam( "hostId" ) String hostId,
                                      @QueryParam( "processId" ) int processPid );

    @GET
    @Path( "container/quota/ram" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRamQuota( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/quota/ram" )
    Response setRamQuota( @FormParam( "containerId" ) String containerId, @FormParam( "ram" ) int ram );

    @GET
    @Path( "container/quota/cpu" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCpuQuota( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/quota/cpu" )
    Response setCpuQuota( @FormParam( "containerId" ) String containerId, @FormParam( "cpu" ) int cpu );

    @GET
    @Path( "container/quota/cpuset" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCpuSet( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/quota/cpuset" )
    Response setCpuSet( @FormParam( "containerId" ) String containerId, @FormParam( "cpuset" ) String cpuSet );

    @GET
    @Path( "container/quota/disk" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getDiskQuota( @QueryParam( "containerId" ) String containerId,
                           @QueryParam( "diskPartition" ) String diskPartition );

    @POST
    @Path( "container/quota/disk" )
    Response setDiskQuota( @FormParam( "containerId" ) String containerId, @FormParam( "diskQuota" ) String diskQuota );
}