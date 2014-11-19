package org.safehaus.subutai.plugin.zookeeper.rest;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


/**
 * REST implementation of Zookeeper API
 */

public class RestServiceImpl implements RestService
{

    private Zookeeper zookeeperManager;

    private EnvironmentManager environmentManager;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
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
            clusterNames.add( config.getClusterName() );
        }
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( clusterNames ) ).build();
    }


    @Override
    public Response getCluster( final String source )
    {
        String clusters = JsonUtil.toJson( zookeeperManager.getCluster( source ) );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
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
            Set<UUID> nodes = new HashSet<>();
            for ( UUID node : expandedConfig.getNodes() )
            {
                nodes.add( node );
            }
            expandedConfig.setNodes( nodes );
        }
        String operationId = wrapUUID( zookeeperManager.installCluster( expandedConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    @Override
    public Response destroyCluster( String clusterName )
    {
        String operationId = wrapUUID( zookeeperManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( zookeeperManager.startNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( zookeeperManager.stopNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( zookeeperManager.destroyNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( zookeeperManager.checkNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response addNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( zookeeperManager.addNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response addNodeStandalone( final String clusterName )
    {
        String operationId = wrapUUID( zookeeperManager.addNode( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
