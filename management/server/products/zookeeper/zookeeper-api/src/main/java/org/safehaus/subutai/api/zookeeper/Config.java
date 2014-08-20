/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.zookeeper;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;


/**
 * @author dilshat
 */
public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Zookeeper";
	private String clusterName = "";
	private int numberOfNodes = 3;
	private Set<Agent> nodes;
	private boolean isStandalone;


	public boolean isStandalone() {
		return isStandalone;
	}


	public void setStandalone(boolean isStandalone) {
		this.isStandalone = isStandalone;
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


	public int getNumberOfNodes() {
		return numberOfNodes;
	}


	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}


	public Set<Agent> getNodes() {
		return nodes;
	}


	public void setNodes(Set<Agent> nodes) {
		this.nodes = nodes;
	}


	@Override
	public String toString() {
		return "Config{" +
				"clusterName='" + clusterName + '\'' +
				", numberOfNodes=" + numberOfNodes +
				", nodes=" + nodes +
				", isStandalone=" + isStandalone +
				'}';
	}
}
