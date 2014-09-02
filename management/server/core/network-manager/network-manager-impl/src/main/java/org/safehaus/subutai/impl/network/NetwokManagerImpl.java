package org.safehaus.subutai.impl.network;

import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.network.NetworkManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.List;

/**
 * Created by daralbaev on 04.04.14.
 */
public class NetwokManagerImpl implements NetworkManager {
	private static CommandRunner commandRunner;

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		NetwokManagerImpl.commandRunner = commandRunner;
	}

	@Override
	public boolean configSshOnAgents(List<Agent> agentList) {
		return new SshManager(agentList).execute();
	}

	@Override
	public boolean configSshOnAgents(List<Agent> agentList, Agent agent) {
		return new SshManager(agentList).execute(agent);
	}

	@Override
	public boolean configHostsOnAgents(List<Agent> agentList, String domainName) {
		return new HostManager(agentList, domainName).execute();
	}

	@Override
	public boolean configHostsOnAgents(List<Agent> agentList, Agent agent, String domainName) {
		return new HostManager(agentList, domainName).execute(agent);
	}
}
