package org.safehaus.subutai.cassandra.services;


import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.Config;
import org.safehaus.subutai.common.JsonUtil;
import org.safehaus.subutai.shared.protocol.Agent;

import javax.ws.rs.core.Response;
import java.util.UUID;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

	private static final String OPERATION_ID = "OPERATION_ID";
	private Cassandra cassandraManager;


	public RestServiceImpl() {
	}

	public Cassandra getCassandraManager() {
		return cassandraManager;
	}

	public void setCassandraManager(Cassandra cassandraManager) {
		this.cassandraManager = cassandraManager;
	}

	@Override
	public String install(String clusterName, String domainName, String numberOfNodes, String numberOfSeeds) {
		Config config = new Config();
		config.setClusterName(clusterName);
		config.setDomainName(domainName);
		config.setNumberOfNodes(Integer.parseInt(numberOfNodes));
		config.setNumberOfSeeds(Integer.parseInt(numberOfSeeds));

		UUID uuid = cassandraManager.installCluster(config);
		return JsonUtil.toJson(OPERATION_ID, uuid.toString());
	}


	@Override
	public String uninstall(String clusterName) {
		UUID uuid = cassandraManager.uninstallCluster(clusterName);
		return JsonUtil.toJson(OPERATION_ID, uuid.toString());
	}


	@Override
	public String startNode(final String clusterName, final String lxchostname) {
		Config cassandraConfig = cassandraManager.getCluster(clusterName);
		if (cassandraConfig != null) {
			Agent agent = null;
			for (Agent node : cassandraConfig.getNodes()) {

				if (node.getHostname().equalsIgnoreCase(lxchostname)) {
					agent = node;
					break;
				}
			}

			if (agent != null) {
				return JsonUtil
						.toJson(OPERATION_ID, cassandraManager.startCassandraService(agent.getUuid().toString()));
			} else {
				return JsonUtil.toJson("ERROR", String.format("Agent %s not found", lxchostname));
			}
		} else {
			return JsonUtil.toJson("ERROR", String.format("Cluster %s not found", clusterName));
		}
	}


	@Override
	public String stopNode(final String clusterName, final String lxchostname) {
		Config cassandraConfig = cassandraManager.getCluster(clusterName);
		if (cassandraConfig != null) {
			Agent agent = null;
			for (Agent node : cassandraConfig.getNodes()) {

				if (node.getHostname().equalsIgnoreCase(lxchostname)) {
					agent = node;
					break;
				}
			}

			if (agent != null) {
				return JsonUtil
						.toJson(OPERATION_ID, cassandraManager.stopCassandraService(agent.getUuid().toString()));
			} else {
				return JsonUtil.toJson("ERROR", String.format("Agent %s not found", lxchostname));
			}
		} else {
			return JsonUtil.toJson("ERROR", String.format("Cluster %s not found", clusterName));
		}
	}


	@Override
	public String checkNode(final String clusterName, final String lxchostname) {
		Config cassandraConfig = cassandraManager.getCluster(clusterName);
		if (cassandraConfig != null) {
			Agent agent = null;
			for (Agent node : cassandraConfig.getNodes()) {

				if (node.getHostname().equalsIgnoreCase(lxchostname)) {
					agent = node;
					break;
				}
			}

			if (agent != null) {
				return JsonUtil
						.toJson(OPERATION_ID, cassandraManager.statusCassandraService(agent.getUuid().toString()));
			} else {
				return JsonUtil.toJson("ERROR", String.format("Agent %s not found", lxchostname));
			}
		} else {
			return JsonUtil.toJson("ERROR", String.format("Cluster %s not found", clusterName));
		}
	}


	public Response installFromJson(final String json) {
		String result = "Json saved : " + json;
		return Response.status(201).entity(result).build();
	}
}