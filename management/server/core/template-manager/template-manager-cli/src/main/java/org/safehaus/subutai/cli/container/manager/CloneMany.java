package org.safehaus.subutai.cli.container.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Command (scope = "container", name = "clone-many")
public class CloneMany extends OsgiCommandSupport {

	TemplateManager templateManager;
	ContainerManager containerManager;
	AgentManager agentManager;

	@Argument (index = 0, required = true)
	private String template;
	@Argument (index = 1, required = true)
	private int nodesCount;
	@Argument (index = 2, required = true)
	private String hosts;

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setContainerManager(ContainerManager containerManager) {
		this.containerManager = containerManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	@Override
	protected Object doExecute() throws Exception {

		Agent a = agentManager.getAgentByHostname(hosts);
		List<Agent> target = getHosts();
		UUID envId = UUID.randomUUID();
		Set<Agent> set = containerManager.clone(envId, template, nodesCount, target);
		if (set.isEmpty()) System.out.println("Result set is empty");
		else System.out.println("Returned clones: " + set.size());
		return null;
	}

	private List<Agent> getHosts() {
		String[] arr = hosts.split("[\\s;,]");
		List<Agent> agents = new ArrayList<>();
		for (String host : arr) {
			if (host.isEmpty()) continue;
			Agent a = agentManager.getAgentByHostname(host);
			if (a != null) agents.add(a);
		}
		return agents;
	}

}
