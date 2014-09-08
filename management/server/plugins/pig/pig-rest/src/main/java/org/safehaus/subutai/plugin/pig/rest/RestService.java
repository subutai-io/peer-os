package org.safehaus.subutai.plugin.pig.rest;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.api.Pig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    public String installCluster( @QueryParam("config") String config )
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

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    //destroy cluster
    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String destroyCluster(
        @PathParam("clusterName") String clusterName
    )
    {
        UUID uuid = pigManager.uninstallCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    //list clusters
    @GET
    @Path("clusters")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getClusters()
    {

        List<Config> configs = pigManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( Config config : configs )
        {
            clusterNames.add( config.getClusterName() );
        }

        return JsonUtil.GSON.toJson( clusterNames );
    }


    //view cluster info
    @GET
    @Path("clusters/{clusterName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCluster( @PathParam("clusterName") String clusterName )
    {
        Config config = pigManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    //destroy node
    @DELETE
    @Path("clusters/{clusterName}/nodes/{lxchostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getCluster(
        @PathParam("clusterName") String clusterName,
        @PathParam("lxchostname") String node
    )
    {
        UUID uuid = pigManager.destroyNode( clusterName, node );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }
}
