package org.safehaus.subutai.plugin.flume.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;

public class RestService {

    private static final String OPERATION_ID = "OPERATION_ID";

    private Flume flumeManager;

    private AgentManager agentManager;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setFlumeManager(Flume flumeManager) {
        this.flumeManager = flumeManager;
    }

    @GET
    @Path("getClusters")
    @Produces({MediaType.APPLICATION_JSON})
    public String getClusters() {

        List<FlumeConfig> configs = flumeManager.getClusters();
        ArrayList<String> clusterNames = new ArrayList();

        for(FlumeConfig config : configs) {
            clusterNames.add(config.getClusterName());
        }

        return JsonUtil.GSON.toJson(clusterNames);
    }

    @GET
    @Path("getCluster")
    @Produces({MediaType.APPLICATION_JSON})
    public String getCluster(
            @QueryParam("clusterName") String clusterName
    ) {
        FlumeConfig config = flumeManager.getCluster(clusterName);

        return JsonUtil.GSON.toJson(config);
    }

    @GET
    @Path("installCluster")
    @Produces({MediaType.APPLICATION_JSON})
    public String installCluster(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("nodes") String nodes
    ) {

        FlumeConfig config = new FlumeConfig();
        config.setClusterName(clusterName);

        String[] arr = nodes.split("[,;]");
        for(String node : arr) {
            Agent agent = agentManager.getAgentByHostname(node);
            if(agent != null) config.getNodes().add(agent);
        }

        UUID uuid = flumeManager.installCluster(config);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("uninstallCluster")
    @Produces({MediaType.APPLICATION_JSON})
    public String uninstallCluster(
            @QueryParam("clusterName") String clusterName
    ) {
        UUID uuid = flumeManager.uninstallCluster(clusterName);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("addNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String addNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("node") String node
    ) {
        UUID uuid = flumeManager.addNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("destroyNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String destroyNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("node") String node
    ) {
        UUID uuid = flumeManager.destroyNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("startNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String startNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("node") String node
    ) {
        UUID uuid = flumeManager.startNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("stopNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String stopNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("node") String node
    ) {
        UUID uuid = flumeManager.stopNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("checkNode")
    @Produces({MediaType.APPLICATION_JSON})
    public String checkNode(
            @QueryParam("clusterName") String clusterName,
            @QueryParam("node") String node
    ) {
        UUID uuid = flumeManager.checkNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }
}
