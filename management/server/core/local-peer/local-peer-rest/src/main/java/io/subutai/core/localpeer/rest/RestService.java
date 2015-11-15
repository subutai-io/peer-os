package io.subutai.core.localpeer.rest;


import java.util.Collection;

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
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
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

    @PUT
    @Path( "update" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response updatePeer( @FormParam( "peer" ) String peer );

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
    @Path( "interfaces" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    HostInterfaces getNetworkInterfaces();

    @POST
    @Path( "n2ntunnel" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void setupN2NConnection( N2NConfig config );

    @DELETE
    @Path( "n2ntunnel/{environmentId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void removeN2NConnection( @PathParam( "environmentId" ) EnvironmentId environmentId );
}