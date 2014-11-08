package org.safehaus.subutai.plugin.storm.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Storm stormManager;
    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setStormManager( Storm stormManager )
    {
        this.stormManager = stormManager;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getClusters()
    {

        List<StormClusterConfiguration> configs = stormManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( StormClusterConfiguration config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
        //        return JsonUtil.GSON.toJson(clusterNames);
    }


    @GET
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName )
    {
        StormClusterConfiguration config = stormManager.getCluster( clusterName );
        String clusterInfo = JsonUtil.GSON.toJson( config );
        return Response.status( Response.Status.OK ).entity( clusterInfo ).build();
        //        return JsonUtil.GSON.toJson(config);
    }


    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @PathParam( "clusterName" ) String clusterName,
                                    @QueryParam( "externalZookeeper" ) boolean externalZookeeper,
                                    @QueryParam( "zookeeperClusterName" ) String zookeeperClusterName,
                                    @QueryParam( "nimbus" ) String nimbus,
                                    @QueryParam( "supervisorsCount" ) String supervisorsCount )
    {

        StormClusterConfiguration config = new StormClusterConfiguration();
        config.setClusterName( clusterName );
        config.setExternalZookeeper( externalZookeeper );
        config.setZookeeperClusterName( zookeeperClusterName );

        Agent nimbusAgent = agentManager.getAgentByHostname( nimbus );
        config.setNimbus( nimbusAgent );

        try
        {
            Integer c = Integer.valueOf( supervisorsCount );
            config.setSupervisorsCount( c );
        }
        catch ( NumberFormatException ex )
        {
            String exception = JsonUtil.toJson( "error", ex.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( exception ).build();
            //            return JsonUtil.toJson("error", ex.getMessage());
        }

        UUID uuid = stormManager.installCluster( config );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
        //        return JsonUtil.toJson(OPERATION_ID, uuid);
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        UUID uuid = stormManager.uninstallCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addNode( @PathParam( "clusterName" ) String clusterName )
    {
        UUID uuid = stormManager.addNode( clusterName );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{hostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyNode( @PathParam( "clusterName" ) String clusterName,
                                 @PathParam( "hostname" ) String hostname )
    {
        UUID uuid = stormManager.destroyNode( clusterName, hostname );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @GET
    @Path( "clusters/{clusterName}/nodes/{hostname}/status" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response statusCheck( @PathParam( "clusterName" ) String clusterName,
                                 @PathParam( "hostname" ) String hostname )
    {
        UUID uuid = stormManager.statusCheck( clusterName, hostname );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{hostname}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startNode( @PathParam( "clusterName" ) String clusterName,
                               @PathParam( "hostname" ) String hostname )
    {
        UUID uuid = stormManager.startNode( clusterName, hostname );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{hostname}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopNode( @PathParam( "clusterName" ) String clusterName, @PathParam( "hostname" ) String hostname )
    {
        UUID uuid = stormManager.stopNode( clusterName, hostname );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{hostname}/restart" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response restartNode( @PathParam( "clusterName" ) String clusterName,
                                 @PathParam( "hostname" ) String hostname )
    {
        UUID uuid = stormManager.restartNode( clusterName, hostname );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
