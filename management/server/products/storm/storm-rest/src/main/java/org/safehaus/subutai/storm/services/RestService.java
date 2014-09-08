package org.safehaus.subutai.storm.services;


import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.api.storm.Storm;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;


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
    @Path("clusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getClusters() {

		List<Config> configs = stormManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configs) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}


	@GET
    @Path("clusters/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
            @PathParam("clusterName") String clusterName
   	) {
		Config config = stormManager.getCluster(clusterName);

		return JsonUtil.GSON.toJson(config);
	}


    @POST
    @Path("clusters/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(
            @PathParam("clusterName") String clusterName,
         	@QueryParam ("externalZookeeper") boolean externalZookeeper,
			@QueryParam ("zookeeperClusterName") String zookeeperClusterName,
			@QueryParam ("nimbus") String nimbus,
			@QueryParam ("supervisorsCount") String supervisorsCount
	) {

		Config config = new Config();
		config.setClusterName(clusterName);
		config.setExternalZookeeper(externalZookeeper);
		config.setZookeeperClusterName(zookeeperClusterName);

		Agent nimbusAgent = agentManager.getAgentByHostname(nimbus);
		config.setNimbus(nimbusAgent);

		try {
			Integer c = Integer.valueOf(supervisorsCount);
			config.setSupervisorsCount(c);
		} catch (NumberFormatException ex) {
			return JsonUtil.toJson("error", ex.getMessage());
		}

		UUID uuid = stormManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


    @DELETE
    @Path("clusters/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(
            @PathParam("clusterName") String clusterName
   	) {
		UUID uuid = stormManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


    @POST
    @Path("clusters/{clusterName}/nodes/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
   	) {
		UUID uuid = stormManager.addNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


    @DELETE
    @Path("clusters/{clusterName}/nodes/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
   	) {
		UUID uuid = stormManager.destroyNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
    @Path("clusters/{clusterName}/nodes/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusCheck(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
   	) {
		UUID uuid = stormManager.statusCheck(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

    @PUT
    @Path("clusters/{clusterName}/nodes/{hostname}/start")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
   	) {
		UUID uuid = stormManager.startNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

    @PUT
    @Path("clusters/{clusterName}/nodes/{hostname}/stop")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
   	) {
		UUID uuid = stormManager.stopNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

    @PUT
    @Path("clusters/{clusterName}/nodes/{hostname}/restart")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String restartNode(
            @PathParam("clusterName") String clusterName,
            @PathParam("hostname") String hostname
   	) {
		UUID uuid = stormManager.restartNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}
}
