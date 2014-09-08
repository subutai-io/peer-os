/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.spark.api;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dilshat
 */
public class SparkClusterConfig implements ConfigBase {

	public static final String PRODUCT_KEY = "Spark";
	private String clusterName = "";

	private Agent masterNode;
	private Set<Agent> slaves;

	public Agent getMasterNode() {
		return masterNode;
	}

	public void setMasterNode(Agent masterNode) {
		this.masterNode = masterNode;
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

	public Set<Agent> getSlaveNodes() {
		return slaves;
	}

	public void setSlaveNodes(Set<Agent> slaves) {
		this.slaves = slaves;
	}

	public Set<Agent> getAllNodes() {
		Set<Agent> allNodes = new HashSet<>();
		if (slaves != null) {
			allNodes.addAll(slaves);
		}
		if (masterNode != null) {
			allNodes.add(masterNode);
		}

		return allNodes;
	}

	@Override
	public String toString() {
		return "Config{" + "clusterName=" + clusterName + ", masterNode=" + masterNode + ", slaves=" + slaves + '}';
	}

}
