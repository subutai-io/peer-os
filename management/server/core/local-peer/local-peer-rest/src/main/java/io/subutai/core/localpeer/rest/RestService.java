package io.subutai.core.localpeer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.util.DateTimeParam;


public interface RestService
{

    @GET
    @Path( "/mhpresent" )
    public Response isMhPresent();

    @GET
    @Path( "/inited" )
    public Response isInited();

    @GET
    @Path( "/ready" )
    public Response isReady();

    @GET
    @Path( "/ping" )
    public Response ping();

    @GET
    @Path( "/info" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public PeerInfo getPeerInfo();


    @POST
    @Path( "tunnels/{environmentId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    Response setupTunnels( @PathParam( "environmentId" ) EnvironmentId environmentId, P2pIps p2pIps );

    @POST
    @Path( "pek" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    PublicKeyContainer createEnvironmentKeyPair( RelationLinkDto environmentId );

    @PUT
    @Path( "pek" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void updateEnvironmentKey( PublicKeyContainer publicKeyContainer );

    @POST
    @Path( "pek/add/{keyId}" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void addInitiatorPeerEnvironmentPubKey( @PathParam( "keyId" ) String keyId, String pek );


    @GET
    @Path( "containers/{environmentId}" )
    Response getEnvironmentContainers( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @GET
    @Path( "resources" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    ResourceHostMetrics getResources();

    @GET
    @Path( "netresources" )
    @Produces( MediaType.APPLICATION_JSON )
    UsedNetworkResources getUsedNetResources();

    @POST
    @Path( "netresources" )
    @Consumes( MediaType.APPLICATION_JSON )
    Integer reserveNetResources( NetworkResourceImpl networkResource );

    @POST
    @Path( "canaccommodate" )
    @Consumes( MediaType.APPLICATION_JSON )
    Boolean canAccommodate( CreateEnvironmentContainersRequest request );

    @POST
    @Path( "p2presetkey" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    void resetP2PSecretKey( P2PCredentials p2PCredentials );

    @POST
    @Path( "p2ptunnel" )
    @Consumes( MediaType.APPLICATION_JSON )
    void joinP2PSwarm( P2PConfig config );

    @PUT
    @Path( "p2ptunnel" )
    @Consumes( MediaType.APPLICATION_JSON )
    void joinOrUpdateP2PSwarm( P2PConfig config );

    @DELETE
    @Path( "cleanup/{environmentId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void cleanupEnvironment( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @POST
    @Path( "alert" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response putAlert( AlertEvent alertEvent );

    @GET
    @Path( "hmetrics/{hostId}/{startTime}/{endTime}" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getHistoricalMetrics( @PathParam( "hostId" ) final String hostId,
                                   @PathParam( "startTime" ) final DateTimeParam startTime,
                                   @PathParam( "endTime" ) final DateTimeParam endTime );

    @GET
    @Path( "metricsseries/{hostId}/{startTime}/{endTime}" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getMetricsSeries( @PathParam( "hostId" ) final String hostId,
                               @PathParam( "startTime" ) final DateTimeParam startTime,
                               @PathParam( "endTime" ) final DateTimeParam endTime );

    @GET
    @Path( "limits/{peerId}" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getResourceLimits( @PathParam( "peerId" ) final PeerId peerId );
}