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
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.ControlNetworkConfig;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.util.DateTimeParam;

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
    public PeerInfo getPeerInfo() throws PeerException;

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


    ;


    @POST
    @Path( "tunnels" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setupTunnels( @FormParam( "peerIps" ) String peerIps, @FormParam( "environmentId" ) String environmentId );

    @POST
    @Path( "pek" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    PublicKeyContainer createEnvironmentKeyPair( /*@PathParam( "userToken" ) String userToken,*/
                                                 EnvironmentId environmentId );

    @PUT
    @Path( "pek" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void updateEnvironmentKey( PublicKeyContainer publicKeyContainer );

    @DELETE
    @Path( "pek/{environmentId}" )
    void removeEnvironmentKeyPair( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @DELETE
    @Path( "network/{environmentId}" )
    void cleanupNetwork( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @PUT
    @Path( "update" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response updatePeer( PeerInfo peerInfo );

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
    @Path( "p2presetkey" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void resetP2PSecretKey( P2PCredentials p2PCredentials );

    @POST
    @Path( "p2ptunnel" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void setupP2PConnection( P2PConfig config );

    @DELETE
    @Path( "p2ptunnel/{environmentId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void removeP2PConnection( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @POST
    @Path( "alert" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response putAlert( AlertEvent alertEvent );

    @GET
    @Path( "hmetrics/{hostname}/{startTime}/{endTime}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getHistoricalMetrics( @PathParam( "hostname" ) final String hostName,
                                   @PathParam( "startTime" ) final DateTimeParam startTime,
                                   @PathParam( "endTime" ) final DateTimeParam endTime );

    @GET
    @Path( "limits/{peerId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getResourceLimits( @PathParam( "peerId" ) final String peerId );

    @GET
    @Path( "control/config/{peerId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getControlNetworkConfig( @PathParam( "peerId" ) final String peerId );

    @PUT
    @Path( "control/update" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response updateControlNetworkConfig( ControlNetworkConfig config );

    @GET
    @Path( "control/{communityName}/{count}/distance/" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getCommunityDistances( @PathParam( "communityName" ) final String communityName,
                                    @PathParam( "count" ) final Integer count );
}