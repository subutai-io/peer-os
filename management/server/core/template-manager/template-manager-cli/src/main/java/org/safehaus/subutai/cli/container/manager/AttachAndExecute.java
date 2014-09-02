package org.safehaus.subutai.cli.container.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.concurrent.TimeUnit;

@Command (scope = "container", name = "attach")
public class AttachAndExecute extends OsgiCommandSupport {

	ContainerManager containerManager;
	AgentManager agentManager;

	@Argument (index = 0, required = true)
	private String hostname;
	@Argument (index = 1, required = true)
	private String cloneName;
	@Argument (index = 2, required = true)
	private String command;
	@Argument (index = 3)
	private int timeoutInSeconds = 60;

	public void setContainerManager(ContainerManager containerManager) {
		this.containerManager = containerManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	@Override
	protected Object doExecute() throws Exception {

		Agent a = agentManager.getAgentByHostname(hostname);
		boolean b = containerManager.attachAndExecute(a, cloneName, command,
				timeoutInSeconds, TimeUnit.SECONDS);
		if (b) System.out.println("Command successfully executed");
		else System.out.println("Failed to execute command");
		return null;
	}

}
