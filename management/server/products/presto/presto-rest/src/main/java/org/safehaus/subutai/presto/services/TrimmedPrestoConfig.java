package org.safehaus.subutai.presto.services;

import java.util.Set;

/**
 * Created by daralbaev on 13.08.14.
 */
public class TrimmedPrestoConfig {
	private String clusterName;

	private String coordinatorHost;
	private Set<String> workersHost;

	public String getClusterName() {
		return clusterName;
	}

	public String getCoordinatorHost() {
		return coordinatorHost;
	}

	public Set<String> getWorkersHost() {
		return workersHost;
	}
}
