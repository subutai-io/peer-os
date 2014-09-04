package org.safehaus.subutai.impl.containermanager;


import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;


/**
 * Contains information for Completion service
 */
public class ContainerInfo {

	private final Agent physicalAgent;
	private final Set<String> lxcHostnames;
	private boolean result;


	public ContainerInfo(final Agent physicalAgent, final Set<String> lxcHostnames) {
		this.physicalAgent = physicalAgent;
		this.lxcHostnames = lxcHostnames;
	}

	public Agent getPhysicalAgent() {
		return physicalAgent;
	}

	public Set<String> getLxcHostnames() {
		return lxcHostnames;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(final boolean result) {
		this.result = result;
	}
}
