package org.safehaus.subutai.lucene.services;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.api.lucene.Lucene;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;


public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Lucene luceneManager;
    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setLuceneManager( Lucene luceneManager ) {
        this.luceneManager = luceneManager;
    }


    @GET
    @Path( "getClusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getClusters() {

        List<Config> configs = luceneManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( Config config : configs ) {
            clusterNames.add( config.getClusterName() );
        }

        return JsonUtil.GSON.toJson( clusterNames );
    }


    @GET
    @Path( "getCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getCluster(
            @QueryParam( "clusterName" ) String clusterName
    ) {
        Config config = luceneManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    @GET
    @Path( "installCluster" )
    @Produces({ MediaType.APPLICATION_JSON })
    public String installCluster(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "nodes" ) List<String> nodes
    ) {

        Config config = new Config();
        config.setClusterName( clusterName );

        for ( String node : nodes ) {
            Agent agent = agentManager.getAgentByHostname( node );
            config.getNodes().add( agent );
        }

        UUID uuid = luceneManager.installCluster( config );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "uninstallCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String uninstallCluster(
            @QueryParam( "clusterName" ) String clusterName
    ) {
        UUID uuid = luceneManager.uninstallCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "addNode" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNode(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "node" ) String node
    ) {
        UUID uuid = luceneManager.addNode( clusterName, node );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "destroyNode" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyNode(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "node" ) String node
    ) {
        UUID uuid = luceneManager.destroyNode( clusterName, node );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }

}
