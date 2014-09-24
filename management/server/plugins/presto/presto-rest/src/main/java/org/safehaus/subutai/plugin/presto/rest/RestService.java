package org.safehaus.subutai.plugin.presto.rest;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Presto prestoManager;
    private AgentManager agentManager;


    public void setPrestoManager( Presto prestoManager )
    {
        this.prestoManager = prestoManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    @GET
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
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
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCluster( @PathParam("clusterName") String clusterName )
    {
        String cluster = JsonUtil.GSON.toJson( prestoManager.getCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    @POST
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response installCluster( @QueryParam("config") String config )
    {
        TrimmedPrestoConfig trimmedPrestoConfig = JsonUtil.GSON.fromJson( config, TrimmedPrestoConfig.class );
        PrestoClusterConfig expandedConfig = new PrestoClusterConfig();

        expandedConfig.setClusterName( trimmedPrestoConfig.getClusterName() );
        expandedConfig
                .setCoordinatorNode( agentManager.getAgentByHostname( trimmedPrestoConfig.getCoordinatorHost() ) );
        if ( trimmedPrestoConfig.getWorkersHost() != null && !trimmedPrestoConfig.getWorkersHost().isEmpty() )
        {
            Set<Agent> nodes = new HashSet<>();
            for ( String node : trimmedPrestoConfig.getWorkersHost() )
            {
                nodes.add( agentManager.getAgentByHostname( node ) );
            }
            expandedConfig.setWorkers( nodes );
        }

        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.installCluster( expandedConfig ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response uninstallCluster( @PathParam("clusterName") String clusterName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.uninstallCluster( clusterName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @POST
    @Path("clusters/{clusterName}/nodes/{lxcHostName}/worker")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addWorkerNode( @PathParam("clusterName") String clusterName,
                                   @PathParam("lxcHostName") String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.addWorkerNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxcHostName}/worker")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response destroyWorkerNode( @PathParam("clusterName") String clusterName,
                                       @PathParam("lxcHostName") String lxcHostName )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, prestoManager.destroyWorkerNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path("clusters/{clusterName}/nodes/{lxcHostName}/coordinator")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response changeCoordinatorNode( @PathParam("clusterName") String clusterName,
                                           @PathParam("lxcHostName") String lxcHostName )
    {
        String operationId =
                JsonUtil.toJson( OPERATION_ID, prestoManager.changeCoordinatorNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path("clusters/{clusterName}/nodes/{lxcHostName}/start")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response startNode( @PathParam("clusterName") String clusterName,
                               @PathParam("lxcHostName") String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.startNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @PUT
    @Path("clusters/{clusterName}/nodes/{lxcHostName}/stop")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response stopNode( @PathParam("clusterName") String clusterName,
                              @PathParam("lxcHostName") String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.stopNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    @GET
    @Path("clusters/{clusterName}/nodes/{lxcHostName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response checkNode( @PathParam("clusterName") String clusterName,
                               @PathParam("lxcHostName") String lxcHostName )
    {
        String operationId = JsonUtil.toJson( OPERATION_ID, prestoManager.checkNode( clusterName, lxcHostName ) );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}