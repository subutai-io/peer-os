/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.shark;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;

/**
 * @author dilshat
 */
public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Shark";
	private String clusterName = "";

	private Set<Agent> nodes;

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

	public Set<Agent> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Agent> nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		return "Config{" + "clusterName=" + clusterName + ", nodes=" + nodes + '}';
	}

}
