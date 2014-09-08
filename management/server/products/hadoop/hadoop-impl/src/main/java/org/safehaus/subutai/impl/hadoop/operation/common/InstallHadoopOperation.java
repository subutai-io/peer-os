package org.safehaus.subutai.impl.hadoop.operation.common;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.impl.hadoop.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/31/14
 * Time: 10:08 PM
 */
public class InstallHadoopOperation {
	private final HadoopClusterConfig hadoopClusterConfig;
	private List<Command> commandList;

	public InstallHadoopOperation(HadoopClusterConfig hadoopClusterConfig ) {

		this.hadoopClusterConfig = hadoopClusterConfig;
		commandList = new ArrayList<Command>();

		commandList.add(Commands.getInstallCommand( hadoopClusterConfig ));
		commandList.add(Commands.getClearMastersCommand( hadoopClusterConfig ));
		commandList.add(Commands.getClearSlavesCommand( hadoopClusterConfig ));
		commandList.add(Commands.getSetMastersCommand( hadoopClusterConfig ));
		commandList.add(Commands.getAddSecondaryNamenodeCommand( hadoopClusterConfig ));
		commandList.add(Commands.getSetDataNodeCommand( hadoopClusterConfig ));
		commandList.add(Commands.getSetTaskTrackerCommand( hadoopClusterConfig ));
		commandList.add(Commands.getFormatNameNodeCommand( hadoopClusterConfig ));
	}

	public List<Command> getCommandList() {
		return commandList;
	}
}
