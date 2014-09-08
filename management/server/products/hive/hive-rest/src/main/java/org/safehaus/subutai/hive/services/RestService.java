package org.safehaus.subutai.hive.services;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.api.hive.Hive;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.protocol.Agent;

import javax.ws.rs.*;
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
	@Path("clusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getClusters() {

		List<Config> configs = hiveManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configs) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}


	@GET
	@Path("cluster/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
			@PathParam("clusterName") String clusterName
	) {
		Config config = hiveManager.getCluster(clusterName);

		return JsonUtil.GSON.toJson(config);
	}


	@POST
	@Path ("clusters/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(
			@PathParam("clusterName") String clusterName,
			@QueryParam ("hadoopClusterName") String hadoopClusterName,
			@QueryParam ("server") String server,
			@QueryParam ("clients") String clients
	) {

		Config config = new Config();
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


	@DELETE
	@Path("clusters/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(
			@PathParam("clusterName") String clusterName
	) {
		UUID uuid = hiveManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@PUT
	@Path("clusters/{clusterName}/nodes/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(
			@PathParam("clusterName") String clusterName,
			@PathParam("hostname") String hostname
	) {
		UUID uuid = hiveManager.addNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@DELETE
	@Path("clusters/{clusterName}/nodes/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyNode(
			@PathParam("clusterName") String clusterName,
			@PathParam("hostname") String hostname
	) {
		UUID uuid = hiveManager.destroyNode(clusterName, hostname);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

}
