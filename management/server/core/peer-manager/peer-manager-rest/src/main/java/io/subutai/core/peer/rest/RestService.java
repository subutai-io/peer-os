package io.subutai.core.peer.rest;


import java.util.Collection;
import java.util.List;

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

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.HostMetric;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.security.PublicKeyContainer;

//todo please check all endpoints for returned media type, do we return correct type if we just return response code
// then no need to indicate it at all!!!


public interface RestService
{
    @Deprecated
    @GET
    @Path( "me" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getLocalPeerInfo();

    @GET
    @Path( "/info" )
    @Produces( MediaType.APPLICATION_JSON )
    public PeerInfo getPeerInfo();

    @GET
    @Path( "registered_peers" )
    @Produces( MediaType.APPLICATION_JSON )
    public List<PeerInfo> getRegisteredPeers();


    @GET
    @Path( "peer_policy" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getPeerPolicy( @QueryParam( "peerId" ) String peerId );


    //todo check why is this endpoint used
    //    @Path( "/" )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    @Deprecated
    //    public Response getRegisteredPeerInfo( @QueryParam( "peerId" ) String peerId );
    //
    //    @GET
    //    @Path( "ping" )
    //    public Response ping();

    @GET
    @Path( "template/get" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getTemplate( @FormParam( "templateName" ) String templateName );

    @GET
    @Path( "vni" )
    @Produces( MediaType.APPLICATION_JSON )
    Collection<Vni> getReservedVnis();

    @POST
    @Path( "vni" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    Vni reserveVni( Vni vni );

    @GET
    @Path( "gateways" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Collection<Gateway> getGateways();


    @POST
    @Path( "container/gateway" )
    Response setDefaultGateway( @FormParam( "containerId" ) String containerId,
                                @FormParam( "gatewayIp" ) String gatewayIp );


    @POST
    @Path( "gateways" )
    Response createGateway( @FormParam( "gatewayIp" ) String gatewayIp, @FormParam( "vlan" ) int vlan );


    @POST
    @Path( "tunnels" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setupTunnels( @FormParam( "peerIps" ) String peerIps, @FormParam( "environmentId" ) String environmentId );

    @POST
    @Path( "pek" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    PublicKeyContainer createEnvironmentKeyPair( EnvironmentId environmentId );

    @DELETE
    @Path( "pek/{environmentId}" )
    void removeEnvironmentKeyPair( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @DELETE
    @Path( "network/{environmentId}" )
    void cleanupNetwork( @PathParam( "environmentId" ) EnvironmentId environmentId );

    //*************** Peer Registration Handshake REST - BEGIN ***************************

    //TODO move all registration process operations to peerManager and remove duplicated code pieces from
    // TODO !!! @Nurkaly do this please !!!
    // PeerRegistrationUI and RestServiceImpl
    //    @POST
    //    @Path( "register" )
    //    @Consumes(     MediaType.APPLICATION_JSON  )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    public Response processRegistrationRequest( RegistrationRequest registrationRequest );

    //
    //    @POST
    //    @Path( "register/{peerIp}" )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    public Response doRegistrationRequest( @PathParam( "peerIp" ) String peerIp );


    //    @DELETE
    //    @Path( "unregister" )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    public Response unregisterPeer( @QueryParam( "peerId" ) String peerId );
    //
    //
    //    @PUT
    //    @Path( "reject" )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    public Response rejectForRegistrationRequest( @FormParam( "rejectedPeerId" ) String rejectedPeerId );
    //
    //
    //    @DELETE
    //    @Path( "remove" )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    public Response removeRegistrationRequest( @QueryParam( "rejectedPeerId" ) String rejectedPeerId );
    //
    //
    //    @PUT
    //    @Path( "approve" )
    //    @Produces(     MediaType.APPLICATION_JSON  )
    //    public Response approveForRegistrationRequest( @FormParam( "approvedPeer" ) String approvedPeer,
    //                                                   @FormParam( "cert" ) String certHEX );


    @PUT
    @Path( "update" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response updatePeer( @FormParam( "peer" ) String peer );

    //*************** Peer Registration Handshake REST - END ***************************


    //*********** Environment Specific REST - BEGIN ***************


//    @POST
//    @Path( "container/destroy" )
//    @Consumes( MediaType.APPLICATION_JSON )
//    @Produces( MediaType.APPLICATION_JSON )
//    public void destroyContainer( ContainerId containerId );
//
//    @POST
//    @Path( "container/start" )
//    @Consumes( MediaType.APPLICATION_JSON )
//    @Produces( MediaType.APPLICATION_JSON )
//    public void startContainer( ContainerId containerId );
//
//    @POST
//    @Path( "container/stop" )
//    @Consumes( MediaType.APPLICATION_JSON )
//    @Produces( MediaType.APPLICATION_JSON )
//    public void stopContainer( ContainerId containerId );
//
//    @POST
//    @Path( "container/state" )
//    @Consumes( MediaType.APPLICATION_JSON )
//    @Produces( MediaType.APPLICATION_JSON )
//    public ContainerHostState getContainerState( ContainerId containerId );
//
//
//    @GET
//    @Path( "container/{id}/usage/{pid}" )
//    @Consumes( MediaType.APPLICATION_JSON )
//    @Produces( MediaType.APPLICATION_JSON )
//    ProcessResourceUsage getProcessResourceUsage( @PathParam( "id" ) ContainerId containerId,
//                                                  @PathParam( "pid" ) int pid );
//
    @GET
    @Path( "container/quota/ram/available" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableRamQuota( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "container/quota/cpu/available" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableCpuQuota( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "container/quota/disk/available" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableDiskQuota( @QueryParam( "containerId" ) String containerId,
                                    @QueryParam( "diskPartition" ) String diskPartition );


    @GET
    @Path( "container/quota/ram" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getRamQuota( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "container/quota/ram/info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getRamQuotaInfo( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/quota/ram" )
    Response setRamQuota( @FormParam( "containerId" ) String containerId, @FormParam( "ram" ) int ram );

    @POST
    @Path( "container/quota/ram2" )
    Response setRamQuota( @FormParam( "containerId" ) String containerId, @FormParam( "ramQuota" ) String ramQuota );

    @GET
    @Path( "container/quota/cpu" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getCpuQuota( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "container/quota/cpu/info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getCpuQuotaInfo( @QueryParam( "containerId" ) String containerId );


    @POST
    @Path( "container/quota/cpu" )
    Response setCpuQuota( @FormParam( "containerId" ) String containerId, @FormParam( "cpu" ) int cpu );

    @GET
    @Path( "container/quota/cpuset" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getCpuSet( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/quota/cpuset" )
    Response setCpuSet( @FormParam( "containerId" ) String containerId, @FormParam( "cpuset" ) String cpuSet );

    @GET
    @Path( "container/quota/disk" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getDiskQuota( @QueryParam( "containerId" ) String containerId,
                           @QueryParam( "diskPartition" ) String diskPartition );

    @POST
    @Path( "container/quota/disk" )
    Response setDiskQuota( @FormParam( "containerId" ) String containerId, @FormParam( "diskQuota" ) String diskQuota );

    @GET
    @Path( "container/info" )
    Response getContainerHostInfoById( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "resources" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    ResourceHostMetrics getResources();

    @GET
    @Path( "metrics/{hostId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    HostMetric getHostMetric( @PathParam( "hostId" ) String hostId );

    //*********** Environment Specific REST - END ***************

    @GET
    @Path( "interfaces" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    HostInterfaces getNetworkInterfaces();

    @POST
    @Path( "n2ntunnel" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void addToTunnel( N2NConfig config );

    @DELETE
    @Path( "n2ntunnel/{environmentId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void removeN2NConnection( @PathParam( "environmentId" ) EnvironmentId environmentId );
}