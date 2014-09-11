package org.safehaus.subutai.plugin.accumulo.rest;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;

import javax.ws.rs.core.Response;
import java.util.*;


/**
 * REST implementation of Accumulo API
 */

public class RestServiceImpl implements RestService
{

    private Accumulo accumuloManager;
    private AgentManager agentManager;


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setAccumuloManager( final Accumulo accumuloManager )
    {
        this.accumuloManager = accumuloManager;
    }


    @Override
    public Response listClusters()
    {
        List<AccumuloClusterConfig> configs = accumuloManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( AccumuloClusterConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.toJson(clusterNames);
        return Response.status(Response.Status.OK).entity(clusters).build();
    }


    @Override
    public Response getCluster(final String clusterName)
    {
        String cluster = JsonUtil.toJson(accumuloManager.getCluster(clusterName));
        return Response.status(Response.Status.OK).entity(cluster).build();
    }


    @Override
    public Response destroyCluster(final String clusterName)
    {
        String operationId = wrapUUID(accumuloManager.uninstallCluster(clusterName));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public Response startCluster(final String clusterName)
    {
        String operationId = wrapUUID(accumuloManager.startCluster(clusterName));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response stopCluster(final String clusterName)
    {
        String operationId = wrapUUID(accumuloManager.stopCluster(clusterName));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response createCluster(final String config)
    {
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
        for ( String tracer : trimmedAccumuloConfig.getTracers() )
        {
            tracers.add( agentManager.getAgentByHostname( tracer ) );
        }
        for ( String slave : trimmedAccumuloConfig.getSlaves() )
        {
            slaves.add( agentManager.getAgentByHostname( slave ) );
        }

        expandedConfig.setTracers( tracers );
        expandedConfig.setSlaves( slaves );

        String operationId = wrapUUID(accumuloManager.installCluster(expandedConfig));
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response addNode(final String clusterName, final String lxcHostname, final String nodeType)
    {
        NodeType accumuloNodeType = NodeType.valueOf(nodeType.toUpperCase());

        String operationId = wrapUUID(accumuloManager.addNode(clusterName, lxcHostname, accumuloNodeType));
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    @Override
    public Response destroyNode(final String clusterName, final String lxcHostname, final String nodeType)
    {
        NodeType accumuloNodeType = NodeType.valueOf(nodeType.toUpperCase());

        String operationId = wrapUUID(accumuloManager.destroyNode(clusterName, lxcHostname, accumuloNodeType));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    @Override
    public Response checkNode(final String clusterName, final String lxcHostname)
    {
        String operationId = wrapUUID(accumuloManager.checkNode(clusterName, lxcHostname));
        return Response.status(Response.Status.OK).entity(operationId).build();
    }
}