package org.safehaus.subutai.product.common.test.unit.mock;


import org.safehaus.subutai.api.agentmanager.AgentListener;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class AgentManagerMock implements AgentManager {

	@Override
	public Set<Agent> getAgents() {
		return null;
	}


	@Override
	public Set<Agent> getPhysicalAgents() {
		return null;
	}


	@Override
	public Set<Agent> getLxcAgents() {
		return null;
	}


	@Override
	public Agent getAgentByHostname(String hostname) {
//        return new Agent(UUID.randomUUID(), hostname, "", "00:00:00:00", Arrays.asList("127.0.0.1", "127.0.0.1"), true, "transportId");
		return null;
	}


	@Override
	public Agent getAgentByUUID(UUID uuid) {
		return null;
	}


	@Override
	public Set<Agent> getLxcAgentsByParentHostname(String parentHostname) {
		return null;
	}


	@Override
	public void addListener(AgentListener listener) {

	}


	@Override
	public void removeListener(AgentListener listener) {

	}
}
