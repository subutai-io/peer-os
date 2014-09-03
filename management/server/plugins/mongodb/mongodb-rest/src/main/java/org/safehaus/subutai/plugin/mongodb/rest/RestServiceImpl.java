package org.safehaus.subutai.plugin.mongodb.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
    public String listClusters()
    {
        List<MongoClusterConfig> configs = mongodbManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( MongoClusterConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        return JsonUtil.toJson( clusterNames );
    }


    @Override
    public String getCluster( final String clusterName )
    {
        return JsonUtil.toJson( mongodbManager.getCluster( clusterName ) );
    }


    @Override
    public String createCluster( final String config )
    {
        TrimmedMongodbConfig mongodbConfig = JsonUtil.fromJson( config, TrimmedMongodbConfig.class );
        MongoClusterConfig expandedConfig = new MongoClusterConfig();
        expandedConfig.setClusterName( mongodbConfig.getClusterName() );
        expandedConfig.setDomainName( mongodbConfig.getDomainName() );
        expandedConfig.setReplicaSetName( mongodbConfig.getReplicaSetName() );
        expandedConfig.setNumberOfConfigServers( mongodbConfig.getNumberOfConfigServers() );
        expandedConfig.setNumberOfRouters( mongodbConfig.getNumberOfRouters() );
        expandedConfig.setNumberOfDataNodes( mongodbConfig.getNumberOfDataNodes() );
        expandedConfig.setCfgSrvPort( mongodbConfig.getCfgSrvPort() );
        expandedConfig.setRouterPort( mongodbConfig.getRouterPort() );
        expandedConfig.setDataNodePort( mongodbConfig.getDataNodePort() );

        return wrapUUID( mongodbManager.installCluster( expandedConfig ) );
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public String destroyCluster( final String clusterName )
    {
        return wrapUUID( mongodbManager.uninstallCluster( clusterName ) );
    }


    @Override
    public String startNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( mongodbManager.startNode( clusterName, lxchostname ) );
    }


    @Override
    public String stopNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( mongodbManager.stopNode( clusterName, lxchostname ) );
    }


    @Override
    public String destroyNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( mongodbManager.destroyNode( clusterName, lxchostname ) );
    }


    @Override
    public String checkNode( final String clusterName, final String lxchostname )
    {
        return wrapUUID( mongodbManager.checkNode( clusterName, lxchostname ) );
    }


    @Override
    public String addNode( final String clusterName, final String nodeType )
    {
        NodeType mongoDbNodeType = NodeType.valueOf( nodeType );
        return wrapUUID( mongodbManager.addNode( clusterName, mongoDbNodeType ) );
    }
}
