package org.safehaus.subutai.core.agentmanager.cli;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;


/**
 * Displays the last log entries
 */
@Command (scope = "agent", name = "get-lxc-agents-by-parent-hostname", description = "get LXC agents by parent hostname")
public class GetLxcAgentsByParentHostnameCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "parentHostname", required = true, multiValued = false,
			description = "Parent hostname")
	String parentHostname;
	private AgentManager agentManager;


	public AgentManager getAgentManager() {
		return agentManager;
	}


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	protected Object doExecute() {

		Set<Agent> agentSet = agentManager.getLxcAgentsByParentHostname(parentHostname);
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
