package org.safehaus.subutai.plugin.accumulo.rest;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;


/**
 * REST implementation of Accumulo API
 */

public class RestServiceImpl implements RestService
{

    private Accumulo accumuloManager;
    private Hadoop hadoop;
    private EnvironmentManager environmentManager;


    @Override
    public Response listClusters()
    {
        List<AccumuloClusterConfig> configs = accumuloManager.getClusters();
        List<String> clusterNames = new ArrayList<>();
        for ( AccumuloClusterConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @Override
    public Response getCluster( final String clusterName )
    {
        String cluster = JsonUtil.toJson( accumuloManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @Override
    public Response createCluster( final String config )
    {
        TrimmedAccumuloConfig trimmedAccumuloConfig = JsonUtil.fromJson( config, TrimmedAccumuloConfig.class );
        AccumuloClusterConfig expandedConfig = new AccumuloClusterConfig();
        expandedConfig.setClusterName( trimmedAccumuloConfig.getClusterName() );
        expandedConfig.setInstanceName( trimmedAccumuloConfig.getInstanceName() );
        expandedConfig.setPassword( trimmedAccumuloConfig.getPassword() );
        expandedConfig.setHadoopClusterName( trimmedAccumuloConfig.getHadoopClusterName() );
        Environment environment = environmentManager
                .getEnvironmentByUUID( hadoop.getCluster( expandedConfig.getHadoopClusterName() ).getEnvironmentId() );
        expandedConfig.setMasterNode(
                environment.getContainerHostByHostname( trimmedAccumuloConfig.getMasterNode() ).getId() );
        expandedConfig.setGcNode( environment.getContainerHostByHostname( trimmedAccumuloConfig.getGcNode() ).getId() );
        expandedConfig
                .setMonitor( environment.getContainerHostByHostname( trimmedAccumuloConfig.getMonitor() ).getId() );

        Set<UUID> tracers = new HashSet<>();
        Set<UUID> slaves = new HashSet<>();
        for ( String tracer : trimmedAccumuloConfig.getTracers() )
        {
            tracers.add( environment.getContainerHostByHostname( tracer ).getId() );
        }
        for ( String slave : trimmedAccumuloConfig.getSlaves() )
        {
            slaves.add( environment.getContainerHostByHostname( slave ).getId() );
        }

        expandedConfig.setTracers( tracers );
        expandedConfig.setSlaves( slaves );

        String operationId = wrapUUID( accumuloManager.installCluster( expandedConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @Override
    public Response destroyCluster( final String clusterName )
    {
        String operationId = wrapUUID( accumuloManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startCluster( final String clusterName )
    {
        String operationId = wrapUUID( accumuloManager.startCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopCluster( final String clusterName )
    {
        String operationId = wrapUUID( accumuloManager.stopCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response addNode( final String clusterName, final String lxcHostname, final String nodeType )
    {
        NodeType accumuloNodeType = NodeType.valueOf( nodeType.toUpperCase() );

        String operationId = wrapUUID( accumuloManager.addNode( clusterName, lxcHostname, accumuloNodeType ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @Override
    public Response destroyNode( final String clusterName, final String lxcHostname, final String nodeType )
    {
        NodeType accumuloNodeType = NodeType.valueOf( nodeType.toUpperCase() );

        String operationId = wrapUUID( accumuloManager.destroyNode( clusterName, lxcHostname, accumuloNodeType ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response checkNode( final String clusterName, final String lxcHostname )
    {
        String operationId = wrapUUID( accumuloManager.checkNode( clusterName, lxcHostname ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    private String wrapUUID( UUID uuid )
    {
        return JsonUtil.toJson( "OPERATION_ID", uuid );
    }


    public Accumulo getAccumuloManager()
    {
        return accumuloManager;
    }


    public void setAccumuloManager( final Accumulo accumuloManager )
    {
        this.accumuloManager = accumuloManager;
    }


    public Hadoop getHadoop()
    {
        return hadoop;
    }


    public void setHadoop( final Hadoop hadoop )
    {
        this.hadoop = hadoop;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }
}