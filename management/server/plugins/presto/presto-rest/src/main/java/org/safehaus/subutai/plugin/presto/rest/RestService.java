package org.safehaus.subutai.plugin.presto.rest;


import java.util.ArrayList;
import java.util.HashSet;
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

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;

import com.google.common.collect.Sets;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Presto prestoManager;
    private Hadoop hadoopManager;
    private EnvironmentManager environmentManager;

    public RestService( final Presto prestoManager, final Hadoop hadoopManager,
                        final EnvironmentManager environmentManager )
    {
        this.prestoManager = prestoManager;
        this.hadoopManager = hadoopManager;
        this.environmentManager = environmentManager;
    }


    public void setPrestoManager( Presto prestoManager )
    {
        this.prestoManager = prestoManager;
    }



    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listClusters()
    {

        List<PrestoClusterConfig> configList = prestoManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( PrestoClusterConfig config : configList )
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
        String cluster = JsonUtil.GSON.toJson( prestoManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @QueryParam( "config" ) String config )
    {
        TrimmedPrestoConfig trimmedPrestoConfig = JsonUtil.GSON.fromJson( config, TrimmedPrestoConfig.class );


        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( trimmedPrestoConfig.getHadoopClusterName() );

        if ( hadoopClusterConfig == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity(
                    String.format( "Hadoop cluster %s not found", trimmedPrestoConfig.getHadoopClusterName() ) ).build();
        }

        Environment environment = environmentManager.getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() );

        if ( environment == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity(
                    String.format( "Environment %s not found", hadoopClusterConfig.getEnvironmentId() ) ).build();
        }

        ContainerHost master = environment.getContainerHostByHostname( trimmedPrestoConfig.getCoordinatorHost() );
        if ( master == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity(
                    String.format( "Master node %s not found", trimmedPrestoConfig.getCoordinatorHost() ) ).build();
        }
        Set<UUID> slaveIds = Sets.newHashSet();
        for ( String slaveHostname : trimmedPrestoConfig.getWorkersHost() )
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
        PrestoClusterConfig expandedConfig = new PrestoClusterConfig();
        expandedConfig.setClusterName( trimmedPrestoConfig.getClusterName() );
        expandedConfig.setHadoopClusterName( trimmedPrestoConfig.getHadoopClusterName() );
        expandedConfig.setSetupType( SetupType.OVER_HADOOP );
        expandedConfig.setCoordinatorNode( master.getId() );
        expandedConfig.getWorkers().addAll( slaveIds );

        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.installCluster( expandedConfig ) );

        return Response.status( Response.Status.ACCEPTED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response uninstallCluster( @PathParam( "clusterName" ) String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/worker" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response addWorkerNode( @PathParam( "clusterName" ) String clusterName,
                                   @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.addWorkerNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/worker" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyWorkerNode( @PathParam( "clusterName" ) String clusterName,
                                       @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, prestoManager.destroyWorkerNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    /*@PUT
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/coordinator" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response changeCoordinatorNode( @PathParam( "clusterName" ) String clusterName,
                                           @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, prestoManager.changeCoordinatorNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }*/


    @PUT
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startNode( @PathParam( "clusterName" ) String clusterName,
                               @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.startNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopNode( @PathParam( "clusterName" ) String clusterName,
                              @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.stopNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @GET
    @Path( "clusters/{clusterName}/nodes/{lxcHostName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response checkNode( @PathParam( "clusterName" ) String clusterName,
                               @PathParam( "lxcHostName" ) String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.checkNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}