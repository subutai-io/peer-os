package org.safehaus.subutai.impl.shark.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.impl.shark.Commands;
import org.safehaus.subutai.impl.shark.SharkImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.UUID;


public class InstallOperationHandler extends AbstractOperationHandler<SharkImpl> {
	private final ProductOperation po;
	private final Config config;

	public InstallOperationHandler(SharkImpl manager, Config config) {
		super(manager, config.getClusterName());
		this.config = config;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Installing %s", Config.PRODUCT_KEY));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {

		if (Strings.isNullOrEmpty(config.getClusterName())) {
			po.addLogFailed("Malformed configuration\nInstallation aborted");
			return;
		}

		if (manager.getCluster(config.getClusterName()) != null) {
			po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
			return;
		}

		org.safehaus.subutai.api.spark.Config sparkConfig
				= manager.getSparkManager().getCluster(config.getClusterName());
		if (sparkConfig == null) {
			po.addLogFailed(String.format("Spark cluster '%s' not found\nInstallation aborted", config.getClusterName()));
			return;
		}

		Config config = new Config();
		config.setClusterName(this.config.getClusterName());
		config.setNodes(sparkConfig.getAllNodes());

		for (Agent node : config.getNodes()) {
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				po.addLogFailed(String.format("Node %s is not connected\nInstallation aborted", node.getHostname()));
				return;
			}
		}

		po.addLog("Checking prerequisites...");

		//check installed ksks packages
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getNodes());
		manager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
			return;
		}

		for (Agent node : config.getNodes()) {
			AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

			if (result.getStdOut().contains("ksks-shark")) {
				po.addLogFailed(String.format("Node %s already has Shark installed\nInstallation aborted", node.getHostname()));
				return;
			} else if (!result.getStdOut().contains("ksks-spark")) {
				po.addLogFailed(String.format("Node %s has no Spark installation\nInstallation aborted", node.getHostname()));
				return;
			}
		}

		po.addLog("Updating db...");
		boolean dbSaveSuccess = manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config);

		if (dbSaveSuccess) {
			po.addLog("Cluster info saved to DB\nInstalling Shark...");

			Command installCommand = Commands.getInstallCommand(config.getNodes());
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				po.addLog("Installation succeeded\nSetting Master IP...");

				Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getNodes(), sparkConfig.getMasterNode());
				manager.getCommandRunner().runCommand(setMasterIPCommand);

				if (setMasterIPCommand.hasSucceeded()) {
					po.addLogDone("Master IP successfully set\nDone");
				} else {
					po.addLogFailed(String.format("Failed to set Master IP, %s", setMasterIPCommand.getAllErrors()));
				}
			} else {
				po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
		}
	}
}
