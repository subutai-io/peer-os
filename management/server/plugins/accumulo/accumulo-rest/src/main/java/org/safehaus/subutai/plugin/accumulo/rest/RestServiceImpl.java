package org.safehaus.subutai.plugin.accumulo.rest;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * REST implementation of Accumulo API
 */

public class RestServiceImpl implements RestService {

    private Accumulo accumuloManager;
    private AgentManager agentManager;


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setAccumuloManager( final Accumulo accumuloManager ) {
        this.accumuloManager = accumuloManager;
    }


    @Override
    public String listClusters() {
        return JsonUtil.toJson( accumuloManager.getClusters() );
    }


    @Override
    public String getCluster( final String source ) {
        return JsonUtil.toJson( accumuloManager.getCluster( source ) );
    }


    @Override
    public String destroyCluster( final String clusterName ) {
        return wrapUUID( accumuloManager.uninstallCluster( clusterName ) );
    }


    private String wrapUUID( UUID uuid ) {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
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
        TrimmedAccumuloConfig trimmedAccumuloConfig = JsonUtil.fromJson( config, TrimmedAccumuloConfig.class );
        AccumuloClusterConfig expandedConfig = new AccumuloClusterConfig();
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


    @Override
    public String checkNode( final String clusterName, final String lxchostname ) {
        return wrapUUID( accumuloManager.checkNode( clusterName, lxchostname ) );
    }
}