package org.safehaus.subutai.plugin.shark.rest;


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import com.google.common.collect.Lists;


public class RestService
{
    private static final String OPERATION_ID = "OPERATION_ID";

    private Shark sharkManager;


    public RestService( final Shark sharkManager )
    {
        this.sharkManager = sharkManager;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listClusters()
    {

        List<SharkClusterConfig> configList = sharkManager.getClusters();
        ArrayList<String> clusterNames = Lists.newArrayList();

        for ( SharkClusterConfig config : configList )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @GET
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName )
    {
        String cluster = JsonUtil.GSON.toJson( sharkManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @PathParam( "clusterName" ) String clusterName )
    {
        SharkClusterConfig sharkConfig = new SharkClusterConfig();

        sharkConfig.setClusterName( clusterName );
        sharkConfig.setSparkClusterName( clusterName );

        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.installCluster( sharkConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addNode( @PathParam( "clusterName" ) String clusterName,
                             @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.addNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyNode( @PathParam( "clusterName" ) String clusterName,
                                 @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.destroyNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "actualize_master_ip/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response actualizeMasterIP( @PathParam( "clusterName" ) String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.actualizeMasterIP( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}