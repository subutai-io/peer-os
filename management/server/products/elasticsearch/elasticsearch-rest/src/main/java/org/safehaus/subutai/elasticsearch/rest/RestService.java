package org.safehaus.subutai.elasticsearch.rest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.elasticsearch.Config;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;
import org.safehaus.subutai.common.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class RestService {

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    private Elasticsearch elasticsearch;
    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setElasticsearch( Elasticsearch elasticsearch ) {
        this.elasticsearch = elasticsearch;
    }


    private String wrapUUID( UUID uuid ) {
        HashMap map = new HashMap();
        map.put( "OPERATION_ID", uuid );

        return gson.toJson( map );
    }


    @GET
    @Path( "listClusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String listClusters() {

        List<Config> configList = elasticsearch.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( Config config : configList ) {
            clusterNames.add( config.getClusterName() );
        }

        return JsonUtil.GSON.toJson( clusterNames );
    }


    @GET
    @Path( "installCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String installCluster(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "numberOfNodes" ) int numberOfNodes,
            @QueryParam( "numberOfMasterNodes" ) int numberOfMasterNodes,
            @QueryParam( "numberOfDataNodes" ) int numberOfDataNodes,
            @QueryParam( "numberOfShards" ) int numberOfShards,
            @QueryParam( "numberOfReplicas" ) int numberOfReplicas
    ) {

        Config config = new Config();
        config.setClusterName( clusterName );
        config.setNumberOfNodes( numberOfNodes );
        config.setNumberOfMasterNodes( numberOfMasterNodes );
        config.setNumberOfDataNodes( numberOfDataNodes );
        config.setNumberOfShards( numberOfShards );
        config.setNumberOfReplicas( numberOfReplicas );

        UUID uuid = elasticsearch.installCluster( config );

        return wrapUUID( uuid );
    }


    @GET
    @Path( "uninstallCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String uninstallCluster(
            @QueryParam( "clusterName" ) String clusterName
    ) {

        System.out.println( ">> " + JsonUtil.toJson( "a", 1 ) );

        return gson.toJson( "done" );
    }
}