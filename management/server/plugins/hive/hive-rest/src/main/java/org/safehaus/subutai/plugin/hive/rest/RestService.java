package org.safehaus.subutai.plugin.hive.rest;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
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

	private Hive hiveManager;

	private AgentManager agentManager;


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void setHiveManager(Hive hiveManager) {
		this.hiveManager = hiveManager;
	}


	@GET
	@Path ("getClusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getClusters() {

		List<HiveConfig> configs = hiveManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (HiveConfig config : configs) {
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
		HiveConfig config = hiveManager.getCluster(clusterName);

		return JsonUtil.GSON.toJson(config);
	}


	@GET
	@Path ("installCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hadoopClusterName") String hadoopClusterName,
			@QueryParam ("server") String server,
			@QueryParam ("clients") String clients
	) {

		HiveConfig config = new HiveConfig();
		config.setClusterName(clusterName);
		config.setHadoopClusterName(hadoopClusterName);

		Agent serverAgent = agentManager.getAgentByHostname(server);
		config.setServer(serverAgent);

		for (String client : clients.split(",")) {
			Agent agent = agentManager.getAgentByHostname(client);
			config.getClients().add(agent);
		}

		UUID uuid = hiveManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("uninstallCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(
			@QueryParam ("clusterName") String clusterName
	) {
		UUID uuid = hiveManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("addNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hostname") String hostname
	) {
		UUID uuid = hiveManager.addNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("destroyNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hostname") String hostname
	) {
		UUID uuid = hiveManager.destroyNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

}
