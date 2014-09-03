package org.safehaus.subutai.impl.hadoop.operation.common;

import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.impl.hadoop.Commands;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daralbaev on 22.04.14.
 */
public class AddNodeOperation {
	private final Config config;
	private List<Command> commandList;
	private Agent agent;

	public AddNodeOperation(Config config, Agent agent) {

		this.config = config;
		this.agent = agent;
		commandList = new ArrayList<Command>();

		commandList.add(Commands.getInstallCommand(agent));
		commandList.add(Commands.getSetMastersCommand(config, agent));
		commandList.add(Commands.getExcludeDataNodeCommand(config, agent));
		commandList.add(Commands.getExcludeTaskTrackerCommand(config, agent));
		commandList.add(Commands.getSetDataNodeCommand(config, agent));
		commandList.add(Commands.getSetTaskTrackerCommand(config, agent));
		commandList.add(Commands.getStartNameNodeCommand(agent));
		commandList.add(Commands.getStartTaskTrackerCommand(agent));
	}

	public List<Command> getCommandList() {
		return commandList;
	}
}
