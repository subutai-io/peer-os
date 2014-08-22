package org.safehaus.subutai.plugin.zookeeper.rest;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.protocol.Agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * REST implementation of Zookeeper API
 */

public class RestServiceImpl implements RestService {


    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Zookeeper zookeeperManager;
    private AgentManager agentManager;


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setZookeeperManager( Zookeeper zookeeperManager ) {
        this.zookeeperManager = zookeeperManager;
    }


    @Override
    public String listClusters() {
        return gson.toJson( zookeeperManager.getClusters() );
    }


    @Override
    public String getCluster( final String source ) {
        return gson.toJson( zookeeperManager.getCluster( source ) );
    }


    @Override
    public String createCluster( String config ) {
        TrimmedZKConfig trimmedZKConfig = gson.fromJson( config, TrimmedZKConfig.class );
        ZookeeperClusterConfig expandedConfig = new ZookeeperClusterConfig();

        expandedConfig.setClusterName( trimmedZKConfig.getClusterName() );
        expandedConfig.setNumberOfNodes( trimmedZKConfig.getNumberOfNodes() );
        expandedConfig.setSetupType( trimmedZKConfig.getSetupType() );
        if ( trimmedZKConfig.getNodes() != null && !trimmedZKConfig.getNodes().isEmpty() ) {
            Set<Agent> nodes = new HashSet<>();
            for ( String node : trimmedZKConfig.getNodes() ) {
                nodes.add( agentManager.getAgentByHostname( node ) );
            }
            expandedConfig.setNodes( nodes );
        }


        return wrapUUID( zookeeperManager.installCluster( expandedConfig ) );
    }


    private String wrapUUID( UUID uuid ) {
        Map map = new HashMap<>();
        map.put( "OPERATION_ID", uuid );
        return gson.toJson( map );
    }


    @Override
    public String destroyCluster( String clusterName ) {
        return wrapUUID( zookeeperManager.uninstallCluster( clusterName ) );
    }


    @Override
    public String startNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( zookeeperManager.startNode( clusterName, lxchostname ) );
    }


    @Override
    public String stopNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( zookeeperManager.stopNode( clusterName, lxchostname ) );
    }


    @Override
    public String destroyNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( zookeeperManager.destroyNode( clusterName, lxchostname ) );
    }


    @Override
    public String checkNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( zookeeperManager.checkNode( clusterName, lxchostname ) );
    }


    @Override
    public String addNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( zookeeperManager.addNode( clusterName, lxchostname ) );
    }


    @Override
    public String addNodeStandalone( final String clusterName ) {
        return wrapUUID( zookeeperManager.addNode( clusterName ) );
    }
}
