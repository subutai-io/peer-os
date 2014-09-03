package org.safehaus.subutai.lucene.services;


import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.api.lucene.Lucene;
import org.safehaus.subutai.common.util.JsonUtil;

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

	private Lucene luceneManager;
	private AgentManager agentManager;


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void setLuceneManager(Lucene luceneManager) {
		this.luceneManager = luceneManager;
	}


	@GET
	@Path ("getClusters")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getClusters() {

		List<Config> configs = luceneManager.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configs) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}


	@GET
	@Path ("getCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String getCluster(@QueryParam ("clusterName") String clusterName) {
		Config config = luceneManager.getCluster(clusterName);

		return JsonUtil.GSON.toJson(config);
	}


	@GET
	@Path ("installCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String installCluster(@QueryParam ("clusterName") String clusterName,
	                             @QueryParam ("hadoopClusterName") String hadoopClusterName,
	                             @QueryParam ("nodes") String nodes) {

		Config config = new Config();
		config.setClusterName(clusterName);
		config.setHadoopClusterName(hadoopClusterName);


		// BUG: Getting the params as list doesn't work. For example "List<String> nodes". To fix this we get a param
		// as plain string and use splitting.
		for (String node : nodes.split(",")) {
			config.getNodes().add(agentManager.getAgentByHostname(node));
		}

		UUID uuid = luceneManager.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("uninstallCluster")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String uninstallCluster(@QueryParam ("clusterName") String clusterName) {
		UUID uuid = luceneManager.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("addNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String addNode(@QueryParam ("clusterName") String clusterName, @QueryParam ("node") String node) {
		UUID uuid = luceneManager.addNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("destroyNode")
	@Produces ( {MediaType.APPLICATION_JSON})
	public String destroyNode(@QueryParam ("clusterName") String clusterName, @QueryParam ("node") String node) {
		UUID uuid = luceneManager.destroyNode(clusterName, node);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}
}
