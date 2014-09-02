package org.safehaus.subutai.plugin.pig.rest;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.api.Pig;
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

	private Pig pigManager;
	private AgentManager agentManager;


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setPigManager(Pig pigManager) {
		this.pigManager = pigManager;
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

		for (String node : nodes.split(",")) {
			Agent agent = agentManager.getAgentByHostname(node);
			config.getNodes().add(agent);
		}

		UUID uuid = pigManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("getClusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getClusters() {

		List<Config> configs = pigManager.getClusters();
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
		Config config = pigManager.getCluster(clusterName);

		return JsonUtil.GSON.toJson(config);
	}


	@GET
	@Path ("destroyNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(
			@QueryParam ("clusterName") String clusterName,
			@QueryParam ("node") String node
	) {
		UUID uuid = pigManager.destroyNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}
}
