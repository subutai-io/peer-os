/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.mongodb;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;
import org.safehaus.subutai.shared.protocol.settings.Common;

import java.util.HashSet;
import java.util.Set;


/**
 * @author dilshat
 */
public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "MongoDB";
	private String clusterName = "";
	private String replicaSetName = "repl";
	private String domainName = Common.DEFAULT_DOMAIN_NAME;
	private int numberOfConfigServers = 3;
	private int numberOfRouters = 2;
	private int numberOfDataNodes = 3;
	private int cfgSrvPort = 27019;
	private int routerPort = 27018;
	private int dataNodePort = 27017;

	private Set<Agent> configServers = new HashSet<>();
	private Set<Agent> routerServers = new HashSet<>();
	;
	private Set<Agent> dataNodes = new HashSet<>();
	;


	public Set<Agent> getAllNodes() {
		Set<Agent> nodes = new HashSet<>();
		if (configServers != null) {
			nodes.addAll(configServers);
		}
		if (dataNodes != null) {
			nodes.addAll(dataNodes);
		}
		if (routerServers != null) {
			nodes.addAll(routerServers);
		}

		return nodes;
	}


	public String getDomainName() {
		return domainName;
	}


	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public int getNumberOfConfigServers() {
		return numberOfConfigServers;
	}

	public void setNumberOfConfigServers(int numberOfConfigServers) {
		this.numberOfConfigServers = numberOfConfigServers;
	}

	public int getNumberOfRouters() {
		return numberOfRouters;
	}

	public void setNumberOfRouters(int numberOfRouters) {
		this.numberOfRouters = numberOfRouters;
	}

	public int getNumberOfDataNodes() {
		return numberOfDataNodes;
	}

	public void setNumberOfDataNodes(int numberOfDataNodes) {
		this.numberOfDataNodes = numberOfDataNodes;
	}

	public String getReplicaSetName() {
		return replicaSetName;
	}

	public void setReplicaSetName(String replicaSetName) {
		this.replicaSetName = replicaSetName;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@Override
	public String getProductName() {
		return PRODUCT_KEY;
	}

	public NodeType getNodeType(Agent node) {
		NodeType nodeType = null;

		if (getRouterServers().contains(node)) {
			nodeType = NodeType.ROUTER_NODE;
		} else if (getConfigServers().contains(node)) {
			nodeType = NodeType.CONFIG_NODE;
		} else if (getDataNodes().contains(node)) {
			nodeType = NodeType.DATA_NODE;
		}

		return nodeType;
	}

	public Set<Agent> getRouterServers() {
		return routerServers;
	}

	public Set<Agent> getConfigServers() {
		return configServers;
	}

	public void setConfigServers(Set<Agent> configServers) {
		this.configServers = configServers;
	}

	public Set<Agent> getDataNodes() {
		return dataNodes;
	}

	public void setDataNodes(Set<Agent> dataNodes) {
		this.dataNodes = dataNodes;
	}

	public void setRouterServers(Set<Agent> routerServers) {
		this.routerServers = routerServers;
	}

	public int getNodePort(Agent node) {

		if (getRouterServers().contains(node)) {
			return getRouterPort();
		} else if (getConfigServers().contains(node)) {
			return getCfgSrvPort();
		}

		return getDataNodePort();
	}

	public int getRouterPort() {
		return routerPort;
	}

	public int getCfgSrvPort() {
		return cfgSrvPort;
	}

	public void setCfgSrvPort(int cfgSrvPort) {
		this.cfgSrvPort = cfgSrvPort;
	}

	public int getDataNodePort() {
		return dataNodePort;
	}

	public void setDataNodePort(int dataNodePort) {
		this.dataNodePort = dataNodePort;
	}

	public void setRouterPort(int routerPort) {
		this.routerPort = routerPort;
	}

	@Override
	public String toString() {
		return "ClusterConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", domainName="
				+ domainName + ", numberOfConfigServers=" + numberOfConfigServers + ", numberOfRouters="
				+ numberOfRouters + ", numberOfDataNodes=" + numberOfDataNodes + ", cfgSrvPort=" + cfgSrvPort
				+ ", routerPort=" + routerPort + ", dataNodePort=" + dataNodePort + ", configServers=" + configServers
				+ ", routerServers=" + routerServers + ", dataNodes=" + dataNodes + '}';
	}
}
