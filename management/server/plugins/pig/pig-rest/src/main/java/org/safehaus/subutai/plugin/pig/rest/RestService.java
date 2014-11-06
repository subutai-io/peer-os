package org.safehaus.subutai.plugin.pig.rest;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.pig.api.Pig;
import org.safehaus.subutai.plugin.pig.api.PigConfig;


public class RestService
{

    private static final String OPERATION_ID = "OPERATION_ID";

    private Pig pigManager;
    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setPigManager( Pig pigManager )
    {
        this.pigManager = pigManager;
    }


    //create cluster
    @POST
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response installCluster( @QueryParam( "config" ) String config )
    {
        TrimmedConfig trimmedConfig = JsonUtil.fromJson( config, TrimmedConfig.class );
        PigConfig pigConfig = new PigConfig();
        pigConfig.setClusterName( trimmedConfig.getClusterName() );

        if ( !CollectionUtil.isCollectionEmpty( trimmedConfig.getNodes() ) )
        {
            Set<UUID> nodes = new HashSet<>();
            for ( String hostname : trimmedConfig.getNodes() )
            {
                nodes.add( UUID.fromString( hostname ) );
            }
            pigConfig.setNodes( nodes );
        }

        UUID uuid = pigManager.installCluster( pigConfig );

        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.CREATED ).entity( operationId ).build();
    }


    //destroy cluster
    @DELETE
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response destroyCluster( @PathParam( "clusterName" ) String clusterName )
    {
        UUID uuid = pigManager.uninstallCluster( clusterName );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }


    //list clusters
    @GET
    @Path( "clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getClusters()
    {

        List<PigConfig> configs = pigManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( PigConfig config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.GSON.toJson( clusterNames );
        return Response.status( Response.Status.OK ).entity( clusters ).build();
    }


    //view cluster info
    @GET
    @Path( "clusters/{clusterName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName )
    {
        PigConfig config = pigManager.getCluster( clusterName );

        String cluster = JsonUtil.GSON.toJson( config );
        return Response.status( Response.Status.OK ).entity( cluster ).build();
    }


    //destroy node
    @DELETE
    @Path( "clusters/{clusterName}/nodes/{lxchostname}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getCluster( @PathParam( "clusterName" ) String clusterName,
                                @PathParam( "lxchostname" ) String node )
    {
        UUID uuid = pigManager.destroyNode( clusterName, node );
        String operationId = JsonUtil.toJson( OPERATION_ID, uuid );
        return Response.status( Response.Status.OK ).entity( operationId ).build();
    }
}
