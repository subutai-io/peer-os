package org.safehaus.subutai.core.peer.rest;


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


public interface RestService
{
    @GET
    @Path( "me" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getSelfPeerInfo();


    @GET
    @Path( "id" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String getId();

    @GET
    @Path( "registered_peers" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegisteredPeers();


    @GET
    @Path( "peer_policy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPeerPolicy( @QueryParam( "peerId" ) String peerId );


    @Path( "/" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRegisteredPeerInfo( @QueryParam( "peerId" ) String peerId );

    @GET
    @Path( "ping" )
    public Response ping();

    @GET
    @Path( "template/get" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTemplate( @FormParam( "templateName" ) String templateName );

    @GET
    @Path( "vni" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getReservedVnis();

    @GET
    @Path( "gateways" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getGateways();

    //*************** Peer Registration Handshake REST - BEGIN ***************************

    @POST
    @Path( "trust_request" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response processTrustRequest( @FormParam( "peer" ) String peer,
                                         @FormParam( "root_cert_px2" ) String root_cert_px2 );

    @POST
    @Path( "trust_response" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response processTrustResponse( @FormParam( "peer" ) String peer,
                                          @FormParam( "root_cert_px2" ) String root_cert_px2,
                                          @FormParam( "status" ) short status );

    @POST
    @Path( "register" )
    @Produces( { MediaType.APPLICATION_JSON } )
    //    public Response processRegisterRequest( @FormParam( "peer" ) String peer,
    //                                            @FormParam( "root_cert_px2" ) String root_cert_px2 );
    public Response processRegisterRequest( @FormParam( "peer" ) String peer );


    @DELETE
    @Path( "unregister" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response unregisterPeer( @QueryParam( "peerId" ) String peerId );


    @PUT
    @Path( "reject" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response rejectForRegistrationRequest( @FormParam( "rejectedPeerId" ) String rejectedPeerId );


    @DELETE
    @Path( "remove" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response removeRegistrationRequest( @QueryParam( "rejectedPeerId" ) String rejectedPeerId );


    @PUT
    @Path( "approve" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response approveForRegistrationRequest( @FormParam( "approvedPeer" ) String approvedPeer,
                                                   @FormParam( "root_cert_px2" ) String root_cert_px2 );


    @PUT
    @Path( "update" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response updatePeer( @FormParam( "peer" ) String peer, @FormParam( "root_cert_px2" ) String root_cert_px2 );

    //*************** Peer Registration Handshake REST - END ***************************


    //*********** Environment Specific REST - BEGIN ***************

    @POST
    @Path( "container/quota" )
    Response setQuota( @FormParam( "containerId" ) String containerId, @FormParam( "quotaInfo" ) String quotaInfo );

    @GET
    @Path( "container/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getQuota( @QueryParam( "containerId" ) String containerId, @QueryParam( "quotaType" ) String quotaType );


    @GET
    @Path( "container/quota/info" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getQuotaInfo( @QueryParam( "containerId" ) String containerId,
                           @QueryParam( "quotaType" ) String quotaType );


    @POST
    @Path( "container/destroy" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyContainer( @FormParam( "containerId" ) String containerId );

    @POST
    @Path( "container/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startContainer( @FormParam( "containerId" ) String containerId );

    @POST
    @Path( "container/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopContainer( @FormParam( "containerId" ) String containerId );

    @POST
    @Path( "container/isconnected" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response isContainerConnected( @FormParam( "containerId" ) String containerId );

    @GET
    @Path( "container/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "container/resource/usage" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getProcessResourceUsage( @QueryParam( "containerId" ) String containerId,
                                      @QueryParam( "processId" ) int processPid );

    @GET
    @Path( "container/quota/ram/available" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getAvailableRamQuota( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "container/quota/cpu/available" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getAvailableCpuQuota( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "container/quota/disk/available" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getAvailableDiskQuota( @QueryParam( "containerId" ) String containerId,
                                    @QueryParam( "diskPartition" ) String diskPartition );


    @GET
    @Path( "container/quota/ram" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRamQuota( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "container/quota/ram/info" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRamQuotaInfo( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/quota/ram" )
    Response setRamQuota( @FormParam( "containerId" ) String containerId, @FormParam( "ram" ) int ram );

    @POST
    @Path( "container/quota/ram2" )
    Response setRamQuota( @FormParam( "containerId" ) String containerId, @FormParam( "ramQuota" ) String ramQuota );

    @GET
    @Path( "container/quota/cpu" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCpuQuota( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "container/quota/cpu/info" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCpuQuotaInfo( @QueryParam( "containerId" ) String containerId );


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

    @POST
    @Path( "container/gateway" )
    Response setDefaultGateway( @FormParam( "containerId" ) String containerId,
                                @FormParam( "gatewayIp" ) String gatewayIp );

    @POST
    @Path( "vni" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response reserveVni( @FormParam( "vni" ) String vni );


    @POST
    @Path( "cert/import" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response importEnvironmentCert( @FormParam( "cert" ) String envCert, @FormParam( "alias" ) String alias );

    @POST
    @Path( "cert/export" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response exportEnvironmentCert( @FormParam( "environmentId" ) String environmentId );


    @DELETE
    @Path( "cert/remove" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response removeEnvironmentCert( @QueryParam( "environmentId" ) String environmentId );

    //*********** Environment Specific REST - END ***************
}