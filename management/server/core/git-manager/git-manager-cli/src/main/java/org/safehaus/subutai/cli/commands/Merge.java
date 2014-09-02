package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.common.protocol.Agent;


/**
 * Merges current branch with specified branch
 */
@Command (scope = "git", name = "merge", description = "Merge current branch with specified branch")
public class Merge extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "branch name", required = false, multiValued = false,
			description = "branch name to merge with (master = default)")
	String branchName;
	private AgentManager agentManager;
	private GitManager gitManager;


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void setGitManager(final GitManager gitManager) {
		this.gitManager = gitManager;
	}


	protected Object doExecute() {

		Agent agent = agentManager.getAgentByHostname(hostname);

		try {
			if (branchName != null) {
				gitManager.merge(agent, repoPath, branchName);
			} else {
				gitManager.merge(agent, repoPath);
			}
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
