package org.safehaus.subutai.cli.aptrepositorymanager;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.shared.protocol.settings.Common;


@Command (scope = "apt", name = "remove-package", description = "Remove package from apt repository by name")
public class RemovePackageCommand extends OsgiCommandSupport {
	@Argument (index = 0, name = "package name", required = true, multiValued = false, description = "name of package")
	String packageName;

	private AptRepositoryManager aptRepositoryManager;
	private AgentManager agentManager;


	public void setAptRepositoryManager(final AptRepositoryManager aptRepositoryManager) {
		this.aptRepositoryManager = aptRepositoryManager;
	}


	public void setAgentManager(final AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	@Override
	protected Object doExecute() {

		try {
			aptRepositoryManager
					.removePackageByName(agentManager.getAgentByHostname(Common.MANAGEMENT_AGENT_HOSTNAME),
							packageName);
		} catch (AptRepoException e) {
			System.out.println(e);
		}
		return null;
	}
}
