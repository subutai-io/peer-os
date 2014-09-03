package org.safehaus.subutai.core.git.cli;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.common.protocol.Agent;


/**
 * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
 */
@Command (scope = "git", name = "undo-hard",
		description = "Bring current branch to the state of the specified remote branch, "
				+ "effectively undoing all local changes")
public class UndoHard extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "branch name", required = false, multiValued = false,
			description = "name of remote branch whose state to restore current branch to (master = default)")
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
				gitManager.undoHard(agent, repoPath, branchName);
			} else {
				gitManager.undoHard(agent, repoPath);
			}
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
