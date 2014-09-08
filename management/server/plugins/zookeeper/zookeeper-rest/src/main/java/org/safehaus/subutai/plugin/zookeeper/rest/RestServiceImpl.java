package org.safehaus.subutai.plugin.zookeeper.rest;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;


/**
 * REST implementation of Zookeeper API
 */

public class RestServiceImpl implements RestService
{

    private Zookeeper zookeeperManager;
    private AgentManager agentManager;


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setZookeeperManager( Zookeeper zookeeperManager )
    {
        this.zookeeperManager = zookeeperManager;
    }


    @Override
    public Response listClusters()
    {
        List<ZookeeperClusterConfig> configs = zookeeperManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( ZookeeperClusterConfig config : configs )
        {
            clusterNames.add(config.getClusterName());
        }
        return Response.ok(JsonUtil.toJson(clusterNames), MediaType.APPLICATION_JSON_TYPE).build();
//        return JsonUtil.toJson( clusterNames );
    }


    @Override
    public Response getCluster( final String source )
    {
        return Response.ok(JsonUtil.toJson(zookeeperManager.getCluster(source)), MediaType.APPLICATION_JSON_TYPE).build();
//        return JsonUtil.toJson( zookeeperManager.getCluster( source ) );
    }


    @Override
    public Response createCluster( String config )
    {
        TrimmedZKConfig trimmedZKConfig = JsonUtil.fromJson( config, TrimmedZKConfig.class );
        ZookeeperClusterConfig expandedConfig = new ZookeeperClusterConfig();

        expandedConfig.setClusterName( trimmedZKConfig.getClusterName() );
        expandedConfig.setNumberOfNodes( trimmedZKConfig.getNumberOfNodes() );
        expandedConfig.setSetupType( trimmedZKConfig.getSetupType() );
        if ( trimmedZKConfig.getNodes() != null && !trimmedZKConfig.getNodes().isEmpty() )
        {
            Set<Agent> nodes = new HashSet<>();
            for ( String node : trimmedZKConfig.getNodes() )
            {
                nodes.add( agentManager.getAgentByHostname( node ) );
            }
            expandedConfig.setNodes( nodes );
        }

        return Response.ok(wrapUUID( zookeeperManager.installCluster( expandedConfig ) )).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public Response destroyCluster( String clusterName )
    {
        return Response.ok(wrapUUID( zookeeperManager.uninstallCluster( clusterName ) )).build();
    }


    @Override
    public Response startNode( final String clusterName, final String lxchostname )
    {
        return Response.ok( wrapUUID( zookeeperManager.startNode( clusterName, lxchostname ) )).build();
    }


    @Override
    public Response stopNode( final String clusterName, final String lxchostname )
    {
        return Response.ok( wrapUUID( zookeeperManager.stopNode( clusterName, lxchostname ) )).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String lxchostname )
    {
        return Response.ok( wrapUUID( zookeeperManager.destroyNode( clusterName, lxchostname ) )).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String lxchostname )
    {
        return Response.ok( wrapUUID( zookeeperManager.checkNode( clusterName, lxchostname ) )).build();
    }


    @Override
    public Response addNode( final String clusterName, final String lxchostname )
    {
        return Response.ok( wrapUUID( zookeeperManager.addNode( clusterName, lxchostname ) )).build();
    }


    @Override
    public Response addNodeStandalone( final String clusterName )
    {
        return Response.ok( wrapUUID( zookeeperManager.addNode( clusterName ) )).build();
    }
}
