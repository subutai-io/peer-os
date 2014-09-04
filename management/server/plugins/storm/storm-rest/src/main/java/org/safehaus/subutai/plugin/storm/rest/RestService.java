package org.safehaus.subutai.plugin.storm.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.storm.api.StormConfig;

public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Storm stormManager;
    private AgentManager agentManager;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setStormManager(Storm stormManager) {
        this.stormManager = stormManager;
    }

    @GET
    @Path("getClusters")
    @Produces({MediaType.APPLICATION_JSON})
    public String getClusters() {

        List<StormConfig> configs = stormManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for(StormConfig config : configs) {
            clusterNames.add(config.getClusterName());
        }

        return JsonUtil.GSON.toJson(clusterNames);
    }

    @GET
    @Path("getCluster")
    @Produces({MediaType.APPLICATION_JSON})
    public String getCluster(@QueryParam("clusterName") String clusterName) {
        StormConfig config = stormManager.getCluster(clusterName);

        return JsonUtil.GSON.toJson(config);
    }

    @GET
    @Path("installCluster")
    @Produces({MediaType.APPLICATION_JSON})
    public String installCluster(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("externalZookeeper") boolean externalZookeeper,
            @QueryParam("zookeeperClusterName") String zookeeperClusterName,
            @QueryParam("nimbus") String nimbus,
            @QueryParam("supervisorsCount") String supervisorsCount
    ) {

        StormConfig config = new StormConfig();
        config.setClusterName(clusterName);
        config.setExternalZookeeper(externalZookeeper);
        config.setZookeeperClusterName(zookeeperClusterName);

        Agent nimbusAgent = agentManager.getAgentByHostname(nimbus);
        config.setNimbus(nimbusAgent);

        try {
            Integer c = Integer.valueOf(supervisorsCount);
            config.setSupervisorsCount(c);
        } catch(NumberFormatException ex) {
            return JsonUtil.toJson("error", ex.getMessage());
        }

        UUID uuid = stormManager.installCluster(config);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("uninstallCluster")
    @Produces({MediaType.APPLICATION_JSON})
    public String uninstallCluster(@QueryParam("clusterName") String clusterName) {
        UUID uuid = stormManager.uninstallCluster(clusterName);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("addNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String addNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("hostname") String hostname
    ) {
        UUID uuid = stormManager.addNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("destroyNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String destroyNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("hostname") String hostname
    ) {
        UUID uuid = stormManager.destroyNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("statusCheck")
    @Produces({MediaType.APPLICATION_JSON})
    public String statusCheck(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("hostname") String hostname
    ) {
        UUID uuid = stormManager.statusCheck(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("startNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String startNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("hostname") String hostname
    ) {
        UUID uuid = stormManager.startNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("stopNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String stopNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("hostname") String hostname
    ) {
        UUID uuid = stormManager.stopNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("restartNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String restartNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("hostname") String hostname
    ) {
        UUID uuid = stormManager.restartNode(clusterName, hostname);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }
}
