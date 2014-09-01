package org.safehaus.subutai.plugin.pig.api;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;
import org.safehaus.subutai.shared.protocol.settings.Common;

import java.util.HashSet;
import java.util.Set;


public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Pig2";
	public static final String PRODUCT_PACKAGE = (Common.PACKAGE_PREFIX + PRODUCT_KEY).toLowerCase();

	private String clusterName = "";
	private Set<Agent> nodes = new HashSet<>();


	public String getClusterName() {
		return clusterName;
	}


	public Config setClusterName(String clusterName) {
		this.clusterName = clusterName;
		return this;
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
