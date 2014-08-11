package org.safehaus.subutai.accumulo.services;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.accumulo.Accumulo;
import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.api.accumulo.NodeType;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.shared.protocol.Agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * REST implementation of Accumulo API
 */

public class RestServiceImpl implements RestService {

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private Accumulo accumuloManager;
    private AgentManager agentManager;


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setAccumuloManager( final Accumulo accumuloManager ) {
        this.accumuloManager = accumuloManager;
    }


    private String wrapUUID( UUID uuid ) {
        Map map = new HashMap<>();
        map.put( "OPERATION_ID", uuid );
        return gson.toJson( map );
    }


    @Override
    public String listClusters() {
        return gson.toJson( accumuloManager.getClusters() );
    }


    @Override
    public String getCluster( final String source ) {
        return gson.toJson( accumuloManager.getCluster( source ) );
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        return wrapUUID( accumuloManager.uninstallCluster( clusterName ) );
    }


    @Override
    public String startCluster( final String clusterName ) {
        return wrapUUID( accumuloManager.startCluster( clusterName ) );
    }


    @Override
    public String stopCluster( final String clusterName ) {
        return wrapUUID( accumuloManager.stopCluster( clusterName ) );
    }


    @Override
    public String createCluster( final String config ) {
        TrimmedAccumuloConfig trimmedAccumuloConfig = gson.fromJson( config, TrimmedAccumuloConfig.class );
        Config expandedConfig = new Config();
        expandedConfig.setClusterName( trimmedAccumuloConfig.getClusterName() );
        expandedConfig.setInstanceName( trimmedAccumuloConfig.getInstanceName() );
        expandedConfig.setPassword( trimmedAccumuloConfig.getPassword() );
        expandedConfig.setMasterNode( agentManager.getAgentByHostname( trimmedAccumuloConfig.getMasterNode() ) );
        expandedConfig.setGcNode( agentManager.getAgentByHostname( trimmedAccumuloConfig.getGcNode() ) );
        expandedConfig.setMonitor( agentManager.getAgentByHostname( trimmedAccumuloConfig.getMonitor() ) );

        Set<Agent> tracers = new HashSet<>();
        Set<Agent> slaves = new HashSet<>();
        for ( String tracer : trimmedAccumuloConfig.getTracers() ) {
            tracers.add( agentManager.getAgentByHostname( tracer ) );
        }
        for ( String slave : trimmedAccumuloConfig.getSlaves() ) {
            slaves.add( agentManager.getAgentByHostname( slave ) );
        }

        expandedConfig.setTracers( tracers );
        expandedConfig.setSlaves( slaves );

        return wrapUUID( accumuloManager.installCluster( expandedConfig ) );
    }


    @Override
    public String addNode( final String clustername, final String lxchostname, final String nodetype ) {
        NodeType accumuloNodeType = NodeType.valueOf( nodetype.toUpperCase() );

        return wrapUUID( accumuloManager.addNode( clustername, lxchostname, accumuloNodeType ) );
    }


    @Override
    public String destroyNode( final String clustername, final String lxchostname, final String nodetype ) {
        NodeType accumuloNodeType = NodeType.valueOf( nodetype.toUpperCase() );

        return wrapUUID( accumuloManager.destroyNode( clustername, lxchostname, accumuloNodeType ) );
    }
}