package org.safehaus.subutai.plugin.hadoop.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public class RestServiceImpl implements RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";
    private Hadoop hadoopManager;
    private EnvironmentManager environmentManager;

    @Override
    public Response listClusters()
    {
        List<HadoopClusterConfig> hadoopClusterConfigList = hadoopManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( HadoopClusterConfig hadoopClusterConfig : hadoopClusterConfigList )
        {
            clusterNames.add( hadoopClusterConfig.getClusterName() );
        }


        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @Override
    public Response getCluster( String clusterName )
    {
        String cluster = JsonUtil.GSON.toJson( hadoopManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @Override
    public Response installCluster( String clusterName, int numberOfSlaveNodes, int numberOfReplicas )
    {
        HadoopClusterConfig hadoopClusterConfig = new HadoopClusterConfig();
        hadoopClusterConfig.setClusterName( clusterName );
        hadoopClusterConfig.setCountOfSlaveNodes( numberOfSlaveNodes );
        hadoopClusterConfig.setReplicationFactor( numberOfReplicas );

        UUID uuid = hadoopManager.installCluster( hadoopClusterConfig );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @Override
    public Response uninstallCluster( String clusterName )
    {
        UUID uuid = hadoopManager.uninstallCluster( clusterName );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startNameNode( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.startNameNode( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopNameNode( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.stopNameNode( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response statusNameNode( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.statusNameNode( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response statusSecondaryNameNode( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId =
                JsonUtil.toJson( OPERATION_ID, hadoopManager.statusSecondaryNameNode( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response startJobTracker( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.startJobTracker( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response stopJobTracker( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.stopJobTracker( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response statusJobTracker( String clusterName )
    {
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.statusJobTracker( hadoopClusterConfig ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response addNode( String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, hadoopManager.addNode( clusterName ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @Override
    public Response statusDataNode( String clusterName, String hostname )
    {
        Agent agent = environmentManager.getEnvironmentByUUID( hadoopManager.getCluster( clusterName ).getEnvironmentId() ).getContainerHostByHostname( hostname ).getAgent();
        String operationId = JsonUtil.toJson( OPERATION_ID,
                hadoopManager.statusDataNode( hadoopManager.getCluster( clusterName ), agent.getHostname() ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @Override
    public Response statusTaskTracker( String clusterName, String hostname )
    {
        Agent agent = environmentManager.getEnvironmentByUUID( hadoopManager.getCluster( clusterName ).getEnvironmentId() ).getContainerHostByHostname( hostname ).getAgent();
        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID,
                hadoopManager.statusTaskTracker( hadoopClusterConfig, agent.getHostname() ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }

    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
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
