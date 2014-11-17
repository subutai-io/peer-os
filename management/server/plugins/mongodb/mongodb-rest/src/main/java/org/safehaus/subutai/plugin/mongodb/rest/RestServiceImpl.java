package org.safehaus.subutai.plugin.mongodb.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;


/**
 * REST implementation of MongoDB API
 */

public class RestServiceImpl implements RestService
{

    private Mongo mongodbManager;


    public void setMongodbManager( Mongo mongodbManager )
    {
        this.mongodbManager = mongodbManager;
    }


    @Override
    public Response listClusters()
    {
        List<MongoClusterConfig> configs = mongodbManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( MongoClusterConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @Override
    public Response getCluster( final String clusterName )
    {
        String cluster = JsonUtil.toJson( mongodbManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @Override
    public Response createCluster( final String config )
    {
        TrimmedMongodbConfig mongodbConfig = JsonUtil.fromJson( config, TrimmedMongodbConfig.class );
        MongoClusterConfig expandedConfig = mongodbManager.newMongoClusterConfigInstance();
        expandedConfig.setClusterName( mongodbConfig.getClusterName() );
        expandedConfig.setDomainName( mongodbConfig.getDomainName() );
        expandedConfig.setReplicaSetName( mongodbConfig.getReplicaSetName() );
        expandedConfig.setNumberOfConfigServers( mongodbConfig.getNumberOfConfigServers() );
        expandedConfig.setNumberOfRouters( mongodbConfig.getNumberOfRouters() );
        expandedConfig.setNumberOfDataNodes( mongodbConfig.getNumberOfDataNodes() );
        expandedConfig.setCfgSrvPort( mongodbConfig.getCfgSrvPort() );
        expandedConfig.setRouterPort( mongodbConfig.getRouterPort() );
        expandedConfig.setDataNodePort( mongodbConfig.getDataNodePort() );

        String operationId = wrapUUID( mongodbManager.installCluster( expandedConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public Response destroyCluster( final String clusterName )
    {
        String operationId = wrapUUID( mongodbManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( mongodbManager.startNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( mongodbManager.stopNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( mongodbManager.destroyNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( mongodbManager.checkNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response addNode( final String clusterName, final String nodeType )
    {
        NodeType mongoDbNodeType = NodeType.valueOf( nodeType );
        String operationId = wrapUUID( mongodbManager.addNode( clusterName, mongoDbNodeType ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
