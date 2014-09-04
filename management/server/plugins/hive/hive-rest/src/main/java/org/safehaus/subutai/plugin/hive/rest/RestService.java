package org.safehaus.subutai.plugin.hive.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;

public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Hive hiveManager;
    private AgentManager agentManager;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setHiveManager(Hive hiveManager) {
        this.hiveManager = hiveManager;
    }

    @GET
    @Path("clusters")
    @Produces({MediaType.APPLICATION_JSON})
    public String getClusters() {

        List<HiveConfig> configs = hiveManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for(HiveConfig config : configs) {
            clusterNames.add(config.getClusterName());
        }

        return JsonUtil.GSON.toJson(clusterNames);
    }

    @GET
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getCluster(@PathParam("clusterName") String clusterName) {
        HiveConfig config = hiveManager.getCluster(clusterName);
        return JsonUtil.GSON.toJson(config);
    }

    @POST
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String installCluster(
            @PathParam("clusterName") String clusterName,
            @QueryParam("hadoopClusterName") String hadoopClusterName,
            @QueryParam("server") String server,
            @QueryParam("clients") String clients
    ) {

        HiveConfig config = new HiveConfig();
        config.setSetupType(SetupType.OVER_HADOOP);
        config.setClusterName(clusterName);
        config.setHadoopClusterName(hadoopClusterName);

        Agent serverAgent = agentManager.getAgentByHostname(server);
        config.setServer(serverAgent);

        for(String client : clients.split(",")) {
            Agent agent = agentManager.getAgentByHostname(client);
            config.getClients().add(agent);
        }

        UUID uuid = hiveManager.installCluster(config);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @POST
    @Path("clusters/{name}/{hadoopName}/{slaveNodesCount}/{replFactor}/{domainName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String install(@PathParam("name") String name,
            @PathParam("hadoopName") String hadoopName,
            @PathParam("slaveNodesCount") String slaveNodesCount,
            @PathParam("replFactor") String replFactor,
            @PathParam("domainName") String domainName) {

        HiveConfig config = new HiveConfig();
        config.setSetupType(SetupType.WITH_HADOOP);
        config.setClusterName(name);
        config.setHadoopClusterName(hadoopName);

        HadoopClusterConfig hc = new HadoopClusterConfig();
        hc.setClusterName(hadoopName);
        if(domainName != null) hc.setDomainName(domainName);
        try {
            int i = Integer.parseInt(slaveNodesCount);
            hc.setCountOfSlaveNodes(i);
        } catch(NumberFormatException ex) {
        }
        try {
            int i = Integer.parseInt(replFactor);
            hc.setReplicationFactor(i);
        } catch(NumberFormatException ex) {
        }

        UUID trackId = hiveManager.installCluster(config, hc);

        return JsonUtil.toJson(OPERATION_ID, trackId);
    }

    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String uninstallCluster(@PathParam("clusterName") String clusterName) {
        UUID uuid = hiveManager.uninstallCluster(clusterName);
        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @POST
    @Path("clusters/{clusterName}/nodes/{hostname}")
    @Produces({MediaType.APPLICATION_JSON})
    public String addNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
    ) {
        UUID uuid = hiveManager.addNode(clusterName, hostname);
        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @DELETE
    @Path("clusters/{clusterName}/nodes/{hostname}")
    @Produces({MediaType.APPLICATION_JSON})
    public String destroyNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
    ) {
        UUID uuid = hiveManager.destroyNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

}
