package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Adds file(s) to commit
 */
@Command (scope = "git", name = "add-files", description = "Add files to commit")
public class AddFiles extends OsgiCommandSupport {

	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
	String hostname;
	@Argument (index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
	String repoPath;
	@Argument (index = 2, name = "file(s)", required = true, multiValued = true, description = "file(s) to add")
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
			gitManager.add(agent, repoPath, new ArrayList<>(files));
		} catch (GitException e) {
			System.out.println(e);
		}

		return null;
	}
}
