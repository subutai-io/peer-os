package org.safehaus.subutai.elasticsearch.rest;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;
import org.safehaus.subutai.shared.protocol.Agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class RestServiceImpl implements RestService {

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


    @Override
    public String listClusters() {
        //                return gson.toJson( accumuloManager.getClusters() );
//        return gson.toJson( "it works!" );
        return "123";
    }
}