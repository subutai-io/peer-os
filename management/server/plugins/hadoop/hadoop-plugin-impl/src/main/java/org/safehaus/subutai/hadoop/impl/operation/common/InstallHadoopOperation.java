package org.safehaus.subutai.hadoop.impl.operation.common;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.hadoop.api.Config;
import org.safehaus.subutai.hadoop.impl.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/31/14
 * Time: 10:08 PM
 */
public class InstallHadoopOperation {
	private final Config config;
	private List<Command> commandList;

	public InstallHadoopOperation(Config config) {

		this.config = config;
		commandList = new ArrayList<>();

		commandList.add(Commands.getInstallCommand(config));
		commandList.add(Commands.getClearMastersCommand(config));
		commandList.add(Commands.getClearSlavesCommand(config));
		commandList.add(Commands.getSetMastersCommand(config));
		commandList.add(Commands.getAddSecondaryNamenodeCommand(config));
		commandList.add(Commands.getSetDataNodeCommand(config));
		commandList.add(Commands.getSetTaskTrackerCommand(config));
		commandList.add(Commands.getFormatNameNodeCommand(config));
	}

	public List<Command> getCommandList() {
		return commandList;
	}
}
