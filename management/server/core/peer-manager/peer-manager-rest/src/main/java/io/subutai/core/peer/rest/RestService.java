package io.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.protocol.N2NConfig;

//todo please check all endpoints for returned media type, do we return correct type if we just return response code
// then no need to indicate it at all!!!


public interface RestService
{
    @GET
    @Path( "me" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getLocalPeerInfo();


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


    //todo check why is this endpoint used
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

    @POST
    @Path( "vni" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response reserveVni( @FormParam( "vni" ) String vni );

    @GET
    @Path( "gateways" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getGateways();

    @POST
    @Path( "gateways" )
    Response createGateway( @FormParam( "gatewayIp" ) String gatewayIp, @FormParam( "vlan" ) int vlan );


    @POST
    @Path( "tunnels" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setupTunnels( @FormParam( "peerIps" ) String peerIps, @FormParam( "environmentId" ) String environmentId );

    //todo remove verbs from urls, http method type should be descriptive say DELETE means remove
    @POST
    @Path( "pek/{environmentId}" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response createEnvironmentKeyPair( @PathParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "pek/{environmentId}" )
    Response removeEnvironmentKeyPair( @PathParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "network/{environmentId}" )
    Response cleanupNetwork( @PathParam( "environmentId" ) String environmentId );

    //*************** Peer Registration Handshake REST - BEGIN ***************************

    //TODO move all registration process operations to peerManager and remove duplicated code pieces from
    // TODO !!! @Nurkaly do this please !!!
    // PeerRegistrationUI and RestServiceImpl
    @POST
    @Path( "register" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response processRegisterRequest( @FormParam( "peer" ) String peer );


    @POST
    @Path( "register/{peerIp}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response sendRegistrationRequest( @PathParam( "peerIp" ) String peerIp );


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
                                                   @FormParam( "cert" ) String certHEX );


    @PUT
    @Path( "update" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response updatePeer( @FormParam( "peer" ) String peer );

    //*************** Peer Registration Handshake REST - END ***************************


    //*********** Environment Specific REST - BEGIN ***************


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


    @GET
    @Path( "container/info" )
    Response getContainerHostInfoById( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "resources" )
    @Produces( { MediaType.APPLICATION_JSON } )
    ResourceHostMetrics getResources();

    //*********** Environment Specific REST - END ***************

    @GET
    @Path( "interfaces" )
    @Produces( { MediaType.APPLICATION_JSON } )
    @Consumes( { MediaType.APPLICATION_JSON } )
    HostInterfaces getNetworkInterfaces();

    @POST
    @Path( "n2ntunnel" )
    @Produces( { MediaType.APPLICATION_JSON } )
    @Consumes( { MediaType.APPLICATION_JSON } )
    Response addToTunnel( N2NConfig config );

    @DELETE
    @Path( "n2ntunnel/{interfaceName}/{communityName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response removeN2NConnection( @PathParam( "interfaceName" ) String interfaceName,
                                  @PathParam( "communityName" ) String communityName );
}