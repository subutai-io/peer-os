package org.safehaus.subutai.hive.services;


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
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.api.hive.Hive;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;


public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Hive hiveManager;

    private AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setHiveManager( Hive hiveManager ) {
        this.hiveManager = hiveManager;
    }


    @GET
    @Path( "getClusters" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String getClusters() {

        List<Config> configs = hiveManager.getClusters();
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
        Config config = hiveManager.getCluster( clusterName );

        return JsonUtil.GSON.toJson( config );
    }


    @GET
    @Path( "installCluster" )
    @Produces({ MediaType.APPLICATION_JSON })
    public String installCluster(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "hadoopClusterName" ) String hadoopClusterName,
            @QueryParam( "server" ) String server,
            @QueryParam( "clients" ) List<String> clients
    ) {

        Config config = new Config();
        config.setClusterName( clusterName );
        config.setClusterName( hadoopClusterName );

        Agent serverAgent = agentManager.getAgentByHostname( server );
        config.setServer( serverAgent );

        for ( String client : clients ) {
            Agent agent = agentManager.getAgentByHostname( client );
            config.getClients().add( agent );
        }

        UUID uuid = hiveManager.installCluster( config );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "uninstallCluster" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String uninstallCluster(
            @QueryParam( "clusterName" ) String clusterName
    ) {
        UUID uuid = hiveManager.uninstallCluster( clusterName );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "addNode" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String addNode(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "hostname" ) String hostname
    ) {
        UUID uuid = hiveManager.addNode( clusterName, hostname );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }


    @GET
    @Path( "destroyNode" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public String destroyNode(
            @QueryParam( "clusterName" ) String clusterName,
            @QueryParam( "hostname" ) String hostname
    ) {
        UUID uuid = hiveManager.destroyNode( clusterName, hostname );

        return JsonUtil.toJson( OPERATION_ID, uuid );
    }

}
