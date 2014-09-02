package org.safehaus.subutai.plugin.flume.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.common.protocol.Agent;

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
        config.setSetupType(SetupType.OVER_HADOOP);
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
    @Path("install/{name}/{hadoopName}/{slaveNodesCount}/{replFactor}/{domainName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String install(@PathParam("name") String name,
            @PathParam("hadoopName") String hadoopName,
            @PathParam("slaveNodesCount") String slaveNodesCount,
            @PathParam("replFactor") String replFactor,
            @PathParam("domainName") String domainName) {

        FlumeConfig config = new FlumeConfig();
        config.setClusterName(name);
        config.setHadoopClusterName(hadoopName);
        config.setSetupType(SetupType.WITH_HADOOP);

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

        UUID trackId = flumeManager.installCluster(config, hc);

        return JsonUtil.toJson(OPERATION_ID, trackId);
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
