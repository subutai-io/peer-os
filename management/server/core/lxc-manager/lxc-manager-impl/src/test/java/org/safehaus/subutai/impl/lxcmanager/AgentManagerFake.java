/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.lxcmanager;


import org.safehaus.subutai.api.agentmanager.AgentListener;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Agent Manager fake class
 */
public class AgentManagerFake implements AgentManager {

	private final Set<Agent> agents = new HashSet<>();


	public AgentManagerFake() {
		agents.add(MockUtils.getPhysicalAgent());
		agents.add(MockUtils.getLxcAgent());
	}


	public Set<Agent> getAgents() {
		return Collections.unmodifiableSet(agents);
	}


	public Set<Agent> getPhysicalAgents() {
		return Util.wrapAgentToSet(MockUtils.getPhysicalAgent());
	}


	public Set<Agent> getLxcAgents() {
		return Util.wrapAgentToSet(MockUtils.getLxcAgent());
	}


	public Agent getAgentByHostname(String hostname) {

		for (Agent agent : agents) {
			if (agent.getHostname().equals(hostname)) {
				return agent;
			}
		}

		return MockUtils.getLxcAgent();
	}


	public Agent getAgentByUUID(UUID uuid) {
		for (Agent agent : agents) {
			if (agent.getUuid().equals(uuid)) {
				return agent;
			}
		}
		return null;
	}


	public Set<Agent> getLxcAgentsByParentHostname(String parentHostname) {
		Set<Agent> lxcAgents = new HashSet<>();
		for (Agent agent : agents) {
			if (agent.getParentHostName().equals(parentHostname)) {
				lxcAgents.add(agent);
			}
		}
		return lxcAgents;
	}


	public void addListener(AgentListener listener) {
	}


	public void removeListener(AgentListener listener) {
	}
}
