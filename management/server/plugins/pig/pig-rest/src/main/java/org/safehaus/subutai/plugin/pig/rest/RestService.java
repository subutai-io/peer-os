package org.safehaus.subutai.plugin.pig.rest;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.api.Pig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;


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
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response installCluster(@QueryParam("config") String config)
    {
        TrimmedConfig trimmedConfig = JsonUtil.fromJson( config, TrimmedConfig.class );
        Config pigConfig = new Config();
        pigConfig.setClusterName( trimmedConfig.getClusterName() );

        if ( !CollectionUtil.isCollectionEmpty( trimmedConfig.getNodes() ) )
        {
            Set<Agent> nodes = new HashSet<>();
            for ( String hostname : trimmedConfig.getNodes() )
            {
                nodes.add( agentManager.getAgentByHostname( hostname ) );
            }
            pigConfig.setNodes( nodes );
        }

        UUID uuid = pigManager.installCluster( pigConfig );

        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.CREATED).entity(operationId).build();
    }


    //destroy cluster
    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response destroyCluster(
        @PathParam("clusterName") String clusterName
    ) {
        UUID uuid = pigManager.uninstallCluster( clusterName );
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }


    //list clusters
    @GET
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getClusters() {

        List<Config> configs = pigManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( Config config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }
        String clusters = JsonUtil.GSON.toJson(clusterNames);
        return Response.status(Response.Status.OK).entity(clusters).build();
    }


    //view cluster info
    @GET
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCluster(@PathParam("clusterName") String clusterName)
    {
        Config config = pigManager.getCluster( clusterName );

        String cluster = JsonUtil.GSON.toJson(config);
        return Response.status(Response.Status.OK).entity(cluster).build();
    }


    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCluster(
        @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String node
    )
    {
        UUID uuid = pigManager.destroyNode( clusterName, node );
        String operationId = JsonUtil.toJson(OPERATION_ID, uuid);
        return Response.status(Response.Status.OK).entity(operationId).build();
    }
}
