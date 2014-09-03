package org.safehaus.subutai.flume.services;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.api.flume.Flume;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.protocol.Agent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
	@Path ("getClusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getClusters() {

		List<Config> configs = flumeManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configs) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}


	@GET
	@Path ("getCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
			@QueryParam ("clusterName") String clusterName
	) {
		Config config = flumeManager.getCluster(clusterName);

		return JsonUtil.GSON.toJson(config);
	}


	@GET
	@Path ("installCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("nodes") String nodes
	) {

		Config config = new Config();
		config.setClusterName(clusterName);

		String[] arr = nodes.split("[,;]");
		for (String node : arr) {
			Agent agent = agentManager.getAgentByHostname(node);
			if (agent != null) config.getNodes().add(agent);
		}

		UUID uuid = flumeManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("uninstallCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(
			@QueryParam ("clusterName") String clusterName
	) {
		UUID uuid = flumeManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("addNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("node") String node
	) {
		UUID uuid = flumeManager.addNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("destroyNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("node") String node
	) {
		UUID uuid = flumeManager.destroyNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("startNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("node") String node
	) {
		UUID uuid = flumeManager.startNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("stopNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("node") String node
	) {
		UUID uuid = flumeManager.stopNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("checkNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String checkNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("node") String node
	) {
		UUID uuid = flumeManager.checkNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}
}
