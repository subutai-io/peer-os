package org.safehaus.subutai.presto.services;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.presto.Config;
import org.safehaus.subutai.api.presto.Presto;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path ("presto")
public class RestService {

	private static final String OPERATION_ID = "OPERATION_ID";

	private Presto prestoManager;
	private AgentManager agentManager;

	public void setPrestoManager(Presto prestoManager) {
		this.prestoManager = prestoManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	@GET
	@Path ("list_clusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String listClusters() {

		List<Config> configList = prestoManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configList) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}

	@GET
	@Path ("get_cluster/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
			@PathParam ("clusterName") String clusterName
	) {
		return JsonUtil.GSON.toJson(prestoManager.getCluster(clusterName));
	}

	@GET
	@Path ("install")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(@QueryParam ("config") String config) {
		TrimmedPrestoConfig trimmedPrestoConfig = JsonUtil.GSON.fromJson(config, TrimmedPrestoConfig.class);
		Config expandedConfig = new Config();

		expandedConfig.setClusterName(trimmedPrestoConfig.getClusterName());
		expandedConfig.setCoordinatorNode(agentManager.getAgentByHostname(trimmedPrestoConfig.getCoordinatorHost()));
		if (trimmedPrestoConfig.getWorkersHost() != null && !trimmedPrestoConfig.getWorkersHost().isEmpty()) {
			Set<Agent> nodes = new HashSet<>();
			for (String node : trimmedPrestoConfig.getWorkersHost()) {
				nodes.add(agentManager.getAgentByHostname(node));
			}
			expandedConfig.setWorkers(nodes);
		}

		return JsonUtil.toJson(OPERATION_ID, prestoManager.installCluster(expandedConfig));
	}

	@GET
	@Path ("uninstall/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(@PathParam ("clusterName") String clusterName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.uninstallCluster(clusterName));
	}

	@GET
	@Path ("add_worker_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addWorkerNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.addWorkerNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("destroy_worker_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyWorkerNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.destroyWorkerNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("change_coordinator_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String changeCoordinatorNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.changeCoordinatorNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("start_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.startNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("stop_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.stopNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("check_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String checkNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, prestoManager.checkNode(clusterName, lxcHostName));
	}
}