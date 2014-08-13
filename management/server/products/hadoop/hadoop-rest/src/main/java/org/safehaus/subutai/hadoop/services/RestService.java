package org.safehaus.subutai.hadoop.services;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;

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

	private Hadoop hadoopManager;
	private AgentManager agentManager;

	public Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	@GET
	@Path ("list_clusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String listClusters() {

		List<Config> configList = hadoopManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configList) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}

	@GET
	@Path ("get_cluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
			@QueryParam ("clusterName") String clusterName
	) {

		UUID uuid = hadoopManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("install_cluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("numberOfSlaveNodes") int numberOfSlaveNodes,
			@QueryParam ("numberOfReplicas") int numberOfReplicas
	) {

		Config config = new Config();
		config.setClusterName(clusterName);
		config.setCountOfSlaveNodes(numberOfSlaveNodes);
		config.setReplicationFactor(numberOfReplicas);

		UUID uuid = hadoopManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("uninstall_cluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(
			@QueryParam ("clusterName") String clusterName
	) {

		UUID uuid = hadoopManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("start_name_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startNameNode(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.startNameNode(config));
	}

	@GET
	@Path ("stop_name_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopNameNode(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.stopNameNode(config));
	}

	@GET
	@Path ("restart_name_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String restartNameNode(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.restartNameNode(config));
	}

	@GET
	@Path ("status_name_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusNameNode(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusNameNode(config));
	}

	@GET
	@Path ("status_secondary_name_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusSecondaryNameNode(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusSecondaryNameNode(config));
	}

	@GET
	@Path ("status_data_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusDataNode(@QueryParam ("hostname") String hostname) {
		Agent agent = agentManager.getAgentByHostname(hostname);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusDataNode(agent));
	}

	@GET
	@Path ("start_job_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startJobTracker(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.startJobTracker(config));
	}

	@GET
	@Path ("stop_job_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopJobTracker(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.stopJobTracker(config));
	}

	@GET
	@Path ("restart_job_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String restartJobTracker(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.restartJobTracker(config));
	}

	@GET
	@Path ("status_job_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusJobTracker(@QueryParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusJobTracker(config));
	}

	@GET
	@Path ("status_task_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusTaskTracker(@QueryParam ("hostname") String hostname) {
		Agent agent = agentManager.getAgentByHostname(hostname);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusTaskTracker(agent));
	}

	@GET
	@Path ("add_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(@QueryParam ("clusterName") String clusterName) {
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.addNode(clusterName));
	}

	@GET
	@Path ("block_data_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String blockDataNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.blockDataNode(config, agent));
	}

	@GET
	@Path ("block_task_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String blockTaskTracker(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.blockTaskTracker(config, agent));
	}

	@GET
	@Path ("unblock_data_node")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String unblockDataNode(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.unblockDataNode(config, agent));
	}

	@GET
	@Path ("unblock_task_tracker")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String unblockTaskTracker(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.unblockTaskTracker(config, agent));
	}
}