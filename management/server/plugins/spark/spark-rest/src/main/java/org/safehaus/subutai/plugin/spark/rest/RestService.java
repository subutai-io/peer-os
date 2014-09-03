package org.safehaus.subutai.plugin.spark.rest;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.common.protocol.Agent;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestService {

	private static final String OPERATION_ID = "OPERATION_ID";

	private Spark sparkManager;
	private AgentManager agentManager;


	public void setSparkManager(final Spark sparkManager) {
		this.sparkManager = sparkManager;
	}


	public void setAgentManager(final AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	@GET
	@Path ("list_clusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String listClusters() {

		List<SparkClusterConfig> configList = sparkManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (SparkClusterConfig config : configList) {
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
		return JsonUtil.GSON.toJson(sparkManager.getCluster(clusterName));
	}

	@GET
	@Path ("install")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(@QueryParam ("config") String config) {
		TrimmedSparkConfig trimmedPrestoConfig = JsonUtil.GSON.fromJson(config, TrimmedSparkConfig.class);
		SparkClusterConfig expandedConfig = new SparkClusterConfig();

		expandedConfig.setClusterName(trimmedPrestoConfig.getClusterName());
		expandedConfig.setMasterNode(agentManager.getAgentByHostname(trimmedPrestoConfig.getMasterNodeHostName()));
		if (trimmedPrestoConfig.getSlavesHostName() != null && !trimmedPrestoConfig.getSlavesHostName().isEmpty()) {
			Set<Agent> nodes = new HashSet<>();
			for (String node : trimmedPrestoConfig.getSlavesHostName()) {
				nodes.add(agentManager.getAgentByHostname(node));
			}
			expandedConfig.setSlaveNodes(nodes);
		}

		return JsonUtil.toJson(OPERATION_ID, sparkManager.installCluster(expandedConfig));
	}

	@GET
	@Path ("uninstall/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(@PathParam ("clusterName") String clusterName) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.uninstallCluster(clusterName));
	}

	@GET
	@Path ("add_slave_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addSlaveNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.addSlaveNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("destroy_slave_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroySlaveNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.destroySlaveNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("change_master_node/{clusterName}/{lxcHostName}/{keepSlave}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String changeMasterNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName,
			@PathParam ("keepSlave") boolean keepSlave) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.changeMasterNode(clusterName, lxcHostName, keepSlave));
	}

	@GET
	@Path ("start_node/{clusterName}/{lxcHostName}/{master}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName,
			@PathParam ("master") boolean master) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.startNode(clusterName, lxcHostName, master));
	}

	@GET
	@Path ("stop_node/{clusterName}/{lxcHostName}/{master}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName,
			@PathParam ("master") boolean master) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.stopNode(clusterName, lxcHostName, master));
	}

	@GET
	@Path ("check_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String checkNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, sparkManager.checkNode(clusterName, lxcHostName));
	}
}