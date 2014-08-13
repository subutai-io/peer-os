package org.safehaus.subutai.storm.services;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.api.storm.Storm;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;


public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Storm stormManager;

    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setStormManager( Storm stormManager ) {
        this.stormManager = stormManager;
    }


    @GET
    @Path( "getClusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getClusters() {

        List<Config> configs = stormManager.getClusters();
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
        Config config = stormManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    @GET
    @Path( "installCluster" )
    @Produces({ MediaType.APPLICATION_JSON })
    public String installCluster(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "externalZookeeper" ) boolean externalZookeeper,
            @QueryParam( "zookeeperClusterName" ) String zookeeperClusterName,
            @QueryParam( "nimbus" ) String nimbus,
            @QueryParam( "supervisors" ) List<String> supervisors
    ) {

        Config config = new Config();
        config.setClusterName( clusterName );
        config.setExternalZookeeper( externalZookeeper );
        config.setZookeeperClusterName( zookeeperClusterName );

        Agent nimbusAgent = agentManager.getAgentByHostname( nimbus );
        config.setNimbus( nimbusAgent );

        config.setSupervisorsCount( supervisors.size() );

        for ( String hostname : supervisors ) {
            Agent agent = agentManager.getAgentByHostname( hostname );
            config.getSupervisors().add( agent );
        }

        UUID uuid = stormManager.installCluster( config );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "uninstallCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String uninstallCluster(
            @QueryParam( "clusterName" ) String clusterName
    ) {
        UUID uuid = stormManager.uninstallCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "addNode" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNode(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "hostname" ) String hostname
    ) {
        UUID uuid = stormManager.addNode( clusterName, hostname );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "destroyNode" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyNode(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "hostname" ) String hostname
    ) {
        UUID uuid = stormManager.destroyNode( clusterName, hostname );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }

}
