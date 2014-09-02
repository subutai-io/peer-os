package org.safehaus.subutai.plugin.lucene.api;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;


public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Lucene2";

	private String hadoopClusterName = "";
	private String clusterName = "";

	private Set<Agent> nodes = new HashSet<>();


	@Override
	public String getClusterName() {
		return clusterName;
	}


	public void setClusterName(final String clusterName) {
		this.clusterName = clusterName;
	}

	@Override
	public String getProductName() {
		return PRODUCT_KEY;
	}

	public String getHadoopClusterName() {
		return hadoopClusterName;
	}

	public void setHadoopClusterName(String hadoopClusterName) {
		this.hadoopClusterName = hadoopClusterName;
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
				"hadoopClusterName='" + hadoopClusterName + '\'' +
				", clusterName='" + clusterName + '\'' +
				", nodes=" + nodes +
				'}';
	}
}
