package org.safehaus.subutai.plugin.shark.impl.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import java.util.UUID;


public class InstallOperationHandler extends AbstractOperationHandler<SharkImpl>
{
	private final SharkClusterConfig config;

	public InstallOperationHandler(SharkImpl manager, SharkClusterConfig config) {
		super(manager, config.getClusterName());
		this.config = config;
		productOperation = manager.getTracker().createProductOperation( SharkClusterConfig.PRODUCT_KEY,
				String.format("Installing %s", SharkClusterConfig.PRODUCT_KEY));
	}

	@Override
	public UUID getTrackerId() {
		return productOperation.getId();
	}

	@Override
	public void run() {

		if (Strings.isNullOrEmpty(config.getClusterName())) {
			productOperation.addLogFailed( "Malformed configuration\nInstallation aborted" );
			return;
		}

		if (manager.getCluster(config.getClusterName()) != null) {
			productOperation.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName() ) );
			return;
		}

		SparkClusterConfig sparkConfig
				= manager.getSparkManager().getCluster(config.getClusterName());
		if (sparkConfig == null) {
			productOperation.addLogFailed( String.format( "Spark cluster '%s' not found\nInstallation aborted", config.getClusterName() ) );
			return;
		}

		SharkClusterConfig config = new SharkClusterConfig();
		config.setClusterName(this.config.getClusterName());
		config.setNodes(sparkConfig.getAllNodes());

		for (Agent node : config.getNodes()) {
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				productOperation.addLogFailed( String.format( "Node %s is not connected\nInstallation aborted", node.getHostname() ) );
				return;
			}
		}

		productOperation.addLog( "Checking prerequisites..." );

		//check installed ksks packages
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getNodes());
		manager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			productOperation.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
			return;
		}

		for (Agent node : config.getNodes()) {
			AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

			if (result.getStdOut().contains("ksks-shark")) {
				productOperation.addLogFailed( String.format( "Node %s already has Shark installed\nInstallation aborted", node.getHostname() ) );
				return;
			} else if (!result.getStdOut().contains("ksks-spark")) {
				productOperation.addLogFailed( String.format( "Node %s has no Spark installation\nInstallation aborted", node.getHostname() ) );
				return;
			}
		}

		productOperation.addLog( "Updating db..." );
		boolean dbSaveSuccess = manager.getDbManager().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config);

		if (dbSaveSuccess) {
			productOperation.addLog( "Cluster info saved to DB\nInstalling Shark..." );

			Command installCommand = Commands.getInstallCommand(config.getNodes());
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				productOperation.addLog( "Installation succeeded\nSetting Master IP..." );

				Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getNodes(), sparkConfig.getMasterNode());
				manager.getCommandRunner().runCommand(setMasterIPCommand);

				if (setMasterIPCommand.hasSucceeded()) {
					productOperation.addLogDone( "Master IP successfully set\nDone" );
				} else {
					productOperation.addLogFailed( String.format( "Failed to set Master IP, %s", setMasterIPCommand.getAllErrors() ) );
				}
			} else {
				productOperation.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
			}
		} else {
			productOperation.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
		}
	}
}
