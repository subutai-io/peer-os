package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Commits file(s)
 */
@Command (scope = "git", name = "commit-files", description = "Commit files")
public class CommitFiles extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "message", required = true, multiValued = false, description = "commit message")
	String message;
	@Argument (index = 3, name = "file(s)", required = true, multiValued = true, description = "file(s) to commit")
	Collection<String> files;
	@Argument (index = 4, name = "conflict resolution", required = false, multiValued = false,
			description = "commit after conflict resolution (true/false = default)")
	boolean afterConflictResolved;
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
			String commitId =
					gitManager.commit(agent, repoPath, new ArrayList<>(files), message, afterConflictResolved);
			System.out.println(String.format("Commit ID : %s", commitId));
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
