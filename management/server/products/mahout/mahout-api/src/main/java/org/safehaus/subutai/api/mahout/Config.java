package org.safehaus.subutai.api.mahout;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;


public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Mahout";
	private String clusterName = "";

	private Set<Agent> nodes = new HashSet();

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
