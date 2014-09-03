package org.safehaus.subutai.core.git.cli;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Undoes all uncommitted changes to specified files
 */
@Command (scope = "git", name = "undo-soft", description = "Undo all uncommitted changes to specified files")
public class UndoSoft extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "file(s)", required = true, multiValued = true,
			description = "file(s) to undo changes to")
	Collection<String> files;
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

			gitManager.undoSoft(agent, repoPath, new ArrayList<>(files));
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
