package org.safehaus.subutai.solr.services;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.api.solr.Solr;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * REST implementation of Solr API
 */

public class RestServiceImpl implements RestService {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Solr solrManager;
	private AgentManager agentManager;


	public void setAgentManager(final AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void setSolrManager(Solr solrManager) {
		this.solrManager = solrManager;
	}

	@Override
	public String listClusters() {
		return gson.toJson(solrManager.getClusters());
	}

	@Override
	public String getCluster(final String clustername) {
		return gson.toJson(solrManager.getCluster(clustername));
	}

	@Override
	public String createCluster(final String config) {
		TrimmedSolrConfig solrConfig = gson.fromJson(config, TrimmedSolrConfig.class);
		Config expandedConfig = new Config();

		expandedConfig.setClusterName(solrConfig.getClusterName());
		expandedConfig.setNumberOfNodes(1);

		return wrapUUID(solrManager.installCluster(expandedConfig));
	}

	private String wrapUUID(UUID uuid) {
		Map map = new HashMap<>();
		map.put("OPERATION_ID", uuid);
		return gson.toJson(map);
	}

	@Override
	public String destroyCluster(final String clusterName) {
		return wrapUUID(solrManager.uninstallCluster(clusterName));
	}


	@Override
	public String startNode(final String clusterName, final String lxchostname) {
		return wrapUUID(solrManager.startNode(clusterName, lxchostname));
	}


	@Override
	public String stopNode(final String clusterName, final String lxchostname) {
		return wrapUUID(solrManager.stopNode(clusterName, lxchostname));
	}


	@Override
	public String destroyNode(final String clusterName, final String lxchostname) {
		return wrapUUID(solrManager.destroyNode(clusterName, lxchostname));
	}


	@Override
	public String checkNode(final String clusterName, final String lxchostname) {
		return wrapUUID(solrManager.checkNode(clusterName, lxchostname));
	}
}
