package org.safehaus.subutai.cli.aptrepositorymanager;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepoException;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.common.settings.Common;


@Command (scope = "apt", name = "add-package", description = "Add package to apt repository by path")
public class AddPackageCommand extends OsgiCommandSupport {
	@Argument (index = 0, name = "package path", required = true, multiValued = false,
			description = "path to package")
	String packagePath;

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
					.addPackageByPath(agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME), packagePath,
							false);
		} catch (AptRepoException e) {
			System.out.println(e);
		}
		return null;
	}
}
