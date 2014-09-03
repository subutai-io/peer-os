package org.safehaus.subutai.core.configuration.impl.command;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.configuration.api.TextInjector;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.FileUtil;


/**
 * Created by bahadyr on 7/17/14.
 */
public class TextInjectorImpl implements TextInjector {

	private CommandRunner commandRunner;
	private AgentManager agentManager;

	public CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(final CommandRunner commandRunner) {
		this.commandRunner = commandRunner;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}


	public void setAgentManager(final AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	@Override
	public boolean echoTextIntoAgent(String hostname, String path, String content) {
		//TODO call echo command on given agent
		Agent agent = agentManager.getAgentByHostname(hostname);
		Command command = Commands.getEchoCommand(agent, path, content);
		commandRunner.runCommand(command);
		System.out.println(hostname + " " + path + " " + content);

		if (command.hasSucceeded()) {
			String config = command.getResults().get(agent.getUuid()).getStdOut();
			System.out.println(config);
		} else {
			//            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
			System.out.println("echo failed!");
			return false;
		}
		return true;
	}


	@Override
	public String catFile(String hostname, String pathToFile) {
		//TODO execute cat commat on given agent and path
		Agent agent = agentManager.getAgentByHostname(hostname);
		Command catCommand = Commands.getCatCommand(agent, pathToFile);
		commandRunner.runCommand(catCommand);

		if (catCommand.hasSucceeded()) {
			//            po.addLog( "cat done" );
			String config = catCommand.getResults().get(agent.getUuid()).getStdOut();
			System.out.println(config);
			System.out.println("cat done!");
		} else {
			//            po.addLogFailed( String.format( "Installation failed, %s", catCommand.getAllErrors() ) );
			System.out.println("cat failed!");
			return "";
		}
		return "";
	}


	@Override
	public String getConfigTemplate(final String path) {
		String content = FileUtil.getContent(path, this);
		return content;
	}
}
