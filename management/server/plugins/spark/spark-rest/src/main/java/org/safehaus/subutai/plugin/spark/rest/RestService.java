package org.safehaus.subutai.plugin.spark.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Spark sparkManager;
    private Hadoop hadoopManager;
    private EnvironmentManager environmentManager;


    public RestService( final Spark sparkManager, final Hadoop hadoopManager,
                        final EnvironmentManager environmentManager )
    {
        this.sparkManager = sparkManager;
        this.hadoopManager = hadoopManager;
        this.environmentManager = environmentManager;
    }


    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listClusters()
    {

        List<SparkClusterConfig> configList = sparkManager.getClusters();
        ArrayList<String> clusterNames = Lists.newArrayList();

        for ( SparkClusterConfig config : configList )
        {
            clusterNames.add( config.getClusterName() );
        }

        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    @GET
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName )
    {
        String cluster = JsonUtil.GSON.toJson( sparkManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @QueryParam( "config" ) String config )
    {
        TrimmedSparkConfig trimmedSparkConfig = JsonUtil.GSON.fromJson( config, TrimmedSparkConfig.class );


        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( trimmedSparkConfig.getHadoopClusterName() );

        if ( hadoopClusterConfig == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity(
                    String.format( "Hadoop cluster %s not found", trimmedSparkConfig.getHadoopClusterName() ) ).build();
        }

        Environment environment = environmentManager.getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() );

        if ( environment == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity(
                    String.format( "Environment %s not found", hadoopClusterConfig.getEnvironmentId() ) ).build();
        }

        ContainerHost master = environment.getContainerHostByHostname( trimmedSparkConfig.getMasterNodeHostName() );
        if ( master == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity(
                    String.format( "Master node %s not found", trimmedSparkConfig.getMasterNodeHostName() ) ).build();
        }
        Set<UUID> slaveIds = Sets.newHashSet();
        for ( String slaveHostname : trimmedSparkConfig.getSlavesHostName() )
        {
            ContainerHost slave = environment.getContainerHostByHostname( slaveHostname );
            if ( slave == null )
            {
                return Response.status( Response.Status.NOT_FOUND )
                               .entity( String.format( "Slave node %s not found", slaveHostname ) ).build();
            }
            slaveIds.add( slave.getId() );
        }
        //fill cluster config
        SparkClusterConfig expandedConfig = new SparkClusterConfig();
        expandedConfig.setClusterName( trimmedSparkConfig.getClusterName() );
        expandedConfig.setHadoopClusterName( trimmedSparkConfig.getHadoopClusterName() );
        expandedConfig.setSetupType( SetupType.OVER_HADOOP );
        expandedConfig.setMasterNodeId( master.getId() );
        expandedConfig.getSlaveIds().addAll( slaveIds );

        String operationId = JsonUtil.toJson( OPERATION_ID, sparkManager.installCluster( expandedConfig ) );

        return Response.status( Response.Status.ACCEPTED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sparkManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addSlaveNode( @PathParam( "clusterName" ) String clusterName,
                                  @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sparkManager.addSlaveNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroySlaveNode( @PathParam( "clusterName" ) String clusterName,
                                      @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sparkManager.destroySlaveNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/{keepSlave}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response changeMasterNode( @PathParam( "clusterName" ) String clusterName,
                                      @PathParam( "lxcHostName" ) String lxcHostName,
                                      @PathParam( "keepSlave" ) boolean keepSlave )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, sparkManager.changeMasterNode( clusterName, lxcHostName, keepSlave ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/{master}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startNode( @PathParam( "clusterName" ) String clusterName,
                               @PathParam( "lxcHostName" ) String lxcHostName, @PathParam( "master" ) boolean master )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, sparkManager.startNode( clusterName, lxcHostName, master ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/{master}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopNode( @PathParam( "clusterName" ) String clusterName,
                              @PathParam( "lxcHostName" ) String lxcHostName, @PathParam( "master" ) boolean master )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, sparkManager.stopNode( clusterName, lxcHostName, master ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @GET
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/{master}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response checkNode( @PathParam( "clusterName" ) String clusterName,
                               @PathParam( "lxcHostName" ) String lxcHostName, @PathParam( "master" ) boolean master )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, sparkManager.checkNode( clusterName, lxcHostName, master ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}