package org.safehaus.subutai.plugin.flume.rest;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.safehaus.subutai.core.agent.api.AgentManager;
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
    @Path("clusters")
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
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getCluster(
            @PathParam("clusterName") String clusterName
    ) {
        FlumeConfig config = flumeManager.getCluster(clusterName);

        return JsonUtil.GSON.toJson(config);
    }

    @POST
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String installCluster(
            @PathParam("clusterName") String clusterName,
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

    @POST
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

    @DELETE
    @Path("clusters/{clusterName}")
    @Produces({MediaType.APPLICATION_JSON})
    public String uninstallCluster(
            @PathParam("clusterName") String clusterName
    ) {
        UUID uuid = flumeManager.uninstallCluster(clusterName);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @POST
    @Path("clusters/{clusterName}/nodes/{node}")
    @Produces({MediaType.APPLICATION_JSON})
    public String addNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = flumeManager.addNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @DELETE
    @Path("clusters/{clusterName}/nodes/{node}")
    @Produces({MediaType.APPLICATION_JSON})
    public String destroyNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = flumeManager.destroyNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @PUT
    @Path("clusters/{clusterName}/nodes/{node}/start")
    @Produces({MediaType.APPLICATION_JSON})
    public String startNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = flumeManager.startNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @PUT
    @Path("clusters/{clusterName}/nodes/{node}/stop")
    @Produces({MediaType.APPLICATION_JSON})
    public String stopNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = flumeManager.stopNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }

    @GET
    @Path("clusters/{clusterName}/nodes/{node}/check")
    @Produces({MediaType.APPLICATION_JSON})
    public String checkNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("node") String node
    ) {
        UUID uuid = flumeManager.checkNode(clusterName, node);

        return JsonUtil.toJson(OPERATION_ID, uuid);
    }
}
