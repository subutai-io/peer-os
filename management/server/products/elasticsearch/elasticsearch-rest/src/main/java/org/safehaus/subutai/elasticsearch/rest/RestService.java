package org.safehaus.subutai.elasticsearch.rest;


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.elasticsearch.Config;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;

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


    @GET
    @Path( "list-clusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String listClusters() {

        List<Config> configList = elasticsearch.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for ( Config config : configList ) {
            clusterNames.add( config.getClusterName() );
        }

        return gson.toJson( clusterNames );
    }
}