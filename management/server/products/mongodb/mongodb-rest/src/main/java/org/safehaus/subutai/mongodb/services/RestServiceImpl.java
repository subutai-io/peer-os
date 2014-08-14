package org.safehaus.subutai.mongodb.services;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.api.mongodb.Mongo;
import org.safehaus.subutai.api.mongodb.NodeType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * REST implementation of MongoDB API
 */

public class RestServiceImpl implements RestService {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Mongo mongodbManager;
    private AgentManager agentManager;


    public void setMongodbManager( Mongo mongodbManager ) {
        this.mongodbManager = mongodbManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    private String wrapUUID( UUID uuid ) {
        Map map = new HashMap<>();
        map.put( "OPERATION_ID", uuid );
        return gson.toJson( map );
    }


    @Override
    public String listClusters() {
        return gson.toJson( mongodbManager.getClusters() );
    }


    @Override
    public String getCluster( final String clusterName ) {
        return gson.toJson( mongodbManager.getCluster( clusterName ) );
    }


    @Override
    public String createCluster( final String config ) {
        TrimmedMongodbConfig mongodbConfig = gson.fromJson( config, TrimmedMongodbConfig.class );
        Config expandedConfig = new Config();
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


    @Override
    public String destroyCluster( final String clusterName ) {
        return wrapUUID( mongodbManager.uninstallCluster( clusterName ) );
    }


    @Override
    public String startNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( mongodbManager.startNode( clusterName, lxchostname ) );
    }


    @Override
    public String stopNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( mongodbManager.stopNode( clusterName, lxchostname ) );
    }


    @Override
    public String destroyNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( mongodbManager.destroyNode( clusterName, lxchostname ) );
    }


    @Override
    public String checkNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( mongodbManager.checkNode( clusterName, lxchostname ) );
    }


    @Override
    public String addNode( final String clusterName, final String nodeType ) {
        NodeType mongoDbNodeType = NodeType.valueOf( nodeType );
        return wrapUUID( mongodbManager.addNode( clusterName, mongoDbNodeType ) );
    }
}
