package org.safehaus.subutai.core.git.cli;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.common.protocol.Agent;


/**
 * Pushes to remote branch
 */
@Command (scope = "git", name = "push", description = "Push to remote branch")
public class Push extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "branch name", required = true, multiValued = false,
			description = "branch name to push to")
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
			gitManager.push(agent, repoPath, branchName);
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
