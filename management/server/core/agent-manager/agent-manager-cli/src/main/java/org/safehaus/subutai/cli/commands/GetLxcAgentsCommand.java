package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;


/**
 * Displays the last log entries
 */
@Command (scope = "agent", name = "get-lxc-agents", description = "get lxc agents")
public class GetLxcAgentsCommand extends OsgiCommandSupport {

	private AgentManager agentManager;


	public AgentManager getAgentManager() {
		return agentManager;
	}


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	protected Object doExecute() {
		Set<Agent> agentSet = agentManager.getLxcAgents();
		StringBuilder sb = new StringBuilder();
		for (Agent agent : agentSet) {
			sb.append("Hostname: ").append(agent.getHostname()).append(" ").append("UUID: ")
					.append(agent.getUuid()).append(" ").append("Parent hostname: ").append(agent.getParentHostName())
					.append(" ").append("MAC address: ").append(agent.getMacAddress()).append(" ").append("IPs: ")
					.append(agent.getListIP()).append(" ").append("\n");
		}

		System.out.println(sb.toString());
		return null;
	}
}
