package org.safehaus.subutai.plugin.elasticsearch.rest;


import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.plugin.elasticsearch.api.*;
import org.safehaus.subutai.common.util.JsonUtil;

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

	private Elasticsearch elasticsearch;

	private AgentManager agentManager;


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void setElasticsearch(Elasticsearch elasticsearch) {
		this.elasticsearch = elasticsearch;
	}


	@GET
	@Path ("listClusters")
	@Produces ({MediaType.APPLICATION_JSON})
	public String listClusters() {

		List<Config> configList = elasticsearch.getClusters();
		ArrayList<String> clusterNames = new ArrayList();

		for (Config config : configList) {
			clusterNames.add(config.getClusterName());
		}

		return JsonUtil.GSON.toJson(clusterNames);
	}


	@POST
	@Path ("installCluster/{clusterName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String installCluster(
            @PathParam("clusterName") String clusterName,
			@QueryParam ("numberOfNodes") int numberOfNodes,
			@QueryParam ("numberOfMasterNodes") int numberOfMasterNodes,
			@QueryParam ("numberOfDataNodes") int numberOfDataNodes,
			@QueryParam ("numberOfShards") int numberOfShards,
			@QueryParam ("numberOfReplicas") int numberOfReplicas
	) {

		Config config = new Config();
		config.setClusterName(clusterName);
		config.setNumberOfNodes(numberOfNodes);
		config.setNumberOfMasterNodes(numberOfMasterNodes);
		config.setNumberOfDataNodes(numberOfDataNodes);
		config.setNumberOfShards(numberOfShards);
		config.setNumberOfReplicas(numberOfReplicas);

		UUID uuid = elasticsearch.installCluster(config);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@DELETE
	@Path ("uninstallCluster/{clusterName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String uninstallCluster(
            @PathParam("clusterName") String clusterName
	) {

		UUID uuid = elasticsearch.uninstallCluster(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@GET
	@Path ("checkAllNodes/{clusterName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String checkAllNodes(
            @PathParam("clusterName") String clusterName
	) {

		UUID uuid = elasticsearch.checkAllNodes(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@PUT
	@Path ("startAllNodes/{clusterName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String startAllNodes(
            @PathParam("clusterName") String clusterName
	) {

		UUID uuid = elasticsearch.startAllNodes(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


	@PUT
	@Path ("stopAllNodes/{clusterName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String stopAllNodes(
            @PathParam("clusterName") String clusterName
	) {

		UUID uuid = elasticsearch.stopAllNodes(clusterName);

		return JsonUtil.toJson(OPERATION_ID, uuid);
	}


    @POST
   	@Path ("addNode/{clusterName}/{node}")
   	@Produces ( {MediaType.APPLICATION_JSON})
   	public String addNode(
   			@PathParam ("clusterName") String clusterName,
   			@PathParam ("node") String node
   	) {
   		UUID uuid = elasticsearch.addNode(clusterName, node);

   		return JsonUtil.toJson(OPERATION_ID, uuid);
   	}


    @DELETE
   	@Path ("destroyNode/{clusterName}/{node}")
   	@Produces ( {MediaType.APPLICATION_JSON})
   	public String destroyNode(
   			@PathParam ("clusterName") String clusterName,
   			@PathParam ("node") String node
   	) {
   		UUID uuid = elasticsearch.destroyNode(clusterName, node);

   		return JsonUtil.toJson(OPERATION_ID, uuid);
   	}
}