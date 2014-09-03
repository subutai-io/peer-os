package org.safehaus.subutai.hadoop.services;

import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.protocol.Agent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RestService {

	private static final String OPERATION_ID = "OPERATION_ID";

	private Hadoop hadoopManager;
	private AgentManager agentManager;

	public void setHadoopManager(Hadoop hadoopManager) {
		this.hadoopManager = hadoopManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
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
	@Path ("get_cluster/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
			@PathParam ("clusterName") String clusterName
	) {
		return JsonUtil.GSON.toJson(hadoopManager.getCluster(clusterName));
	}

	@GET
	@Path ("install_cluster/{clusterName}/{numberOfSlaveNodes}/{numberOfReplicas}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("numberOfSlaveNodes") int numberOfSlaveNodes,
			@PathParam ("numberOfReplicas") int numberOfReplicas
	) {

		Config config = new Config();
		config.setClusterName(clusterName);
		config.setCountOfSlaveNodes(numberOfSlaveNodes);
		config.setReplicationFactor(numberOfReplicas);

		UUID uuid = hadoopManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("uninstall_cluster/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(
			@PathParam ("clusterName") String clusterName
	) {

		UUID uuid = hadoopManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}

	@GET
	@Path ("start_name_node/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startNameNode(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.startNameNode(config));
	}

	@GET
	@Path ("stop_name_node/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopNameNode(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.stopNameNode(config));
	}

	@GET
	@Path ("restart_name_node/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String restartNameNode(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.restartNameNode(config));
	}

	@GET
	@Path ("status_name_node/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusNameNode(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusNameNode(config));
	}

	@GET
	@Path ("status_secondary_name_node/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusSecondaryNameNode(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusSecondaryNameNode(config));
	}

	@GET
	@Path ("status_data_node/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusDataNode(@PathParam ("hostname") String hostname) {
		Agent agent = agentManager.getAgentByHostname(hostname);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusDataNode(agent));
	}

	@GET
	@Path ("start_job_tracker/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String startJobTracker(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.startJobTracker(config));
	}

	@GET
	@Path ("stop_job_tracker/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String stopJobTracker(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.stopJobTracker(config));
	}

	@GET
	@Path ("restart_job_tracker/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String restartJobTracker(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.restartJobTracker(config));
	}

	@GET
	@Path ("status_job_tracker/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusJobTracker(@PathParam ("clusterName") String clusterName) {
		Config config = hadoopManager.getCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusJobTracker(config));
	}

	@GET
	@Path ("status_task_tracker/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String statusTaskTracker(@PathParam ("hostname") String hostname) {
		Agent agent = agentManager.getAgentByHostname(hostname);
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.statusTaskTracker(agent));
	}

	@GET
	@Path ("add_node/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(@PathParam ("clusterName") String clusterName) {
		return JsonUtil.toJson(OPERATION_ID, hadoopManager.addNode(clusterName));
	}

	@GET
	@Path ("block_data_node/{clusterName}/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String blockDataNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.blockDataNode(config, agent));
	}

	@GET
	@Path ("block_task_tracker/{clusterName}/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String blockTaskTracker(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.blockTaskTracker(config, agent));
	}

	@GET
	@Path ("unblock_data_node/{clusterName}/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String unblockDataNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.unblockDataNode(config, agent));
	}

	@GET
	@Path ("unblock_task_tracker/{clusterName}/{hostname}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String unblockTaskTracker(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("hostname") String hostname) {
		Config config = hadoopManager.getCluster(clusterName);
		Agent agent = agentManager.getAgentByHostname(hostname);

		return JsonUtil.toJson(OPERATION_ID, hadoopManager.unblockTaskTracker(config, agent));
	}
}