package org.safehaus.subutai.spark.services;

import java.util.Set;

/**
 * Created by daralbaev on 13.08.14.
 */
public class TrimmedSparkConfig {

	private String clusterName;

	private String masterNodeHostName;
	private Set<String> slavesHostName;

	public String getClusterName() {
		return clusterName;
	}

	public String getMasterNodeHostName() {
		return masterNodeHostName;
	}

	public Set<String> getSlavesHostName() {
		return slavesHostName;
	}
}
