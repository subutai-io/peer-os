package org.safehaus.subutai.core.network.api;

import org.safehaus.subutai.common.protocol.Agent;

import java.util.List;

public interface NetworkManager {
	public boolean configSshOnAgents(List<Agent> agentList);

	public boolean configSshOnAgents(List<Agent> agentList, Agent agent);

	public boolean configHostsOnAgents(List<Agent> agentList, String domainName);

	public boolean configHostsOnAgents(List<Agent> agentList, Agent agent, String domainName);
}
