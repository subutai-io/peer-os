package org.safehaus.subutai.shark.services;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.api.shark.Shark;
import org.safehaus.subutai.api.spark.Spark;
import org.safehaus.subutai.common.JsonUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

public class RestService {
	private static final String OPERATION_ID = "OPERATION_ID";

	private Shark sharkManager;
	private Spark sparkManager;
	private AgentManager agentManager;

	public void setSharkManager(Shark sharkManager) {
		this.sharkManager = sharkManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setSparkManager(Spark sparkManager) {
		this.sparkManager = sparkManager;
	}

	@GET
	@Path ("list_clusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String listClusters() {

		List<Config> configList = sharkManager.getClusters();
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
		return JsonUtil.GSON.toJson(sharkManager.getCluster(clusterName));
	}

	@GET
	@Path ("install")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(@PathParam ("clusterName") String clusterName) {
		org.safehaus.subutai.api.spark.Config sparkConfig = sparkManager.getCluster(clusterName);
		Config sharkConfig = new Config();

		sharkConfig.setClusterName(sparkConfig.getClusterName());
		sharkConfig.setNodes(sparkConfig.getAllNodes());

		return JsonUtil.toJson(OPERATION_ID, sharkManager.installCluster(sharkConfig));
	}

	@GET
	@Path ("uninstall/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(@PathParam ("clusterName") String clusterName) {
		return JsonUtil.toJson(OPERATION_ID, sharkManager.uninstallCluster(clusterName));
	}

	@GET
	@Path ("add_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, sharkManager.addNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("destroy_node/{clusterName}/{lxcHostName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyNode(
			@PathParam ("clusterName") String clusterName,
			@PathParam ("lxcHostName") String lxcHostName) {
		return JsonUtil.toJson(OPERATION_ID, sharkManager.destroyNode(clusterName, lxcHostName));
	}

	@GET
	@Path ("actualize_master_ip/{clusterName}")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String actualizeMasterIP(
			@PathParam ("clusterName") String clusterName) {
		return JsonUtil.toJson(OPERATION_ID, sharkManager.actualizeMasterIP(clusterName));
	}
}