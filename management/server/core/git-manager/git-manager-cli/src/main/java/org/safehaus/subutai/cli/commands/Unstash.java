package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Applies all stashed changes to current branch
 */
@Command (scope = "git", name = "unstash", description = "Apply all stashed changes to current branch")
public class Unstash extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "stashName", required = true, multiValued = false,
			description = "stash name to apply")
	String stashName;

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
			gitManager.unstash(agent, repoPath, stashName);
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
