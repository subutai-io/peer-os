package org.safehaus.subutai.plugin.shark.rest;


import com.google.common.collect.Lists;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import javax.ws.rs.core.Response;
import java.util.List;


public class RestServiceImpl implements RestServiceInterface
{
    private static final String OPERATION_ID = "OPERATION_ID";

    private Shark sharkManager;


    public RestServiceImpl( final Shark sharkManager )
    {
        this.sharkManager = sharkManager;
    }

    @Override
    public Response listClusters()
    {

        List<SharkClusterConfig> configList = sharkManager.getClusters();
        List<String> clusterNames = Lists.newArrayList();

        for ( SharkClusterConfig config : configList )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }

    @Override
    public Response getCluster( String clusterName )
    {
        String cluster = JsonUtil.GSON.toJson( sharkManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }

    @Override
    public Response installCluster( String clusterName )
    {
        SharkClusterConfig sharkConfig = new SharkClusterConfig();

        sharkConfig.setClusterName( clusterName );
        sharkConfig.setSparkClusterName( clusterName );

        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.installCluster( sharkConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }

    @Override
    public Response uninstallCluster( String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }

    @Override
    public Response addNode(  String clusterName, String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.addNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }

    @Override
    public Response destroyNode(  String clusterName, String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.destroyNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }

    @Override
    public Response actualizeMasterIP( String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sharkManager.actualizeMasterIP( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}