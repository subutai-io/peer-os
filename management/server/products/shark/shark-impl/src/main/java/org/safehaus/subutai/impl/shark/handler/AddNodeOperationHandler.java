package org.safehaus.subutai.impl.shark.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.impl.shark.Commands;
import org.safehaus.subutai.impl.shark.SharkImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<SharkImpl> {
	private final ProductOperation po;
	private final String lxcHostname;

	public AddNodeOperationHandler(SharkImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Adding node to %s", clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		//check if node agent is connected
		Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (agent == null) {
			po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", lxcHostname));
			return;
		}

		if (config.getNodes().contains(agent)) {
			po.addLogFailed(String.format("Node %s already belongs to this cluster\nOperation aborted", lxcHostname));
			return;
		}

		org.safehaus.subutai.api.spark.Config sparkConfig
				= manager.getSparkManager().getCluster(clusterName);
		if (sparkConfig == null) {
			po.addLogFailed(String.format("Spark cluster '%s' not found\nInstallation aborted", clusterName));
			return;
		}

		if (!sparkConfig.getAllNodes().contains(agent)) {
			po.addLogFailed(String.format("Node %s does not belong to %s spark cluster\nOperation aborted", lxcHostname, clusterName));
			return;
		}

		po.addLog("Checking prerequisites...");

		//check installed ksks packages
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(agent));
		manager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
			return;
		}

		AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

		if (result.getStdOut().contains("ksks-shark")) {
			po.addLogFailed(String.format("Node %s already has Shark installed\nInstallation aborted", lxcHostname));
			return;
		} else if (!result.getStdOut().contains("ksks-spark")) {
			po.addLogFailed(String.format("Node %s has no Spark installation\nInstallation aborted", lxcHostname));
			return;
		}

		config.getNodes().add(agent);
		po.addLog("Updating db...");
		//save to db
		if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLog("Cluster info updated in DB\nInstalling Shark...");

			Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(agent));
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				po.addLog("Installation succeeded\nSetting Master IP...");

				Command setMasterIPCommand = Commands.getSetMasterIPCommand(Util.wrapAgentToSet(agent), sparkConfig.getMasterNode());
				manager.getCommandRunner().runCommand(setMasterIPCommand);

				if (setMasterIPCommand.hasSucceeded()) {
					po.addLogDone("Master IP set successfully\nDone");
				} else {
					po.addLogFailed(String.format("Failed to set Master IP, %s", setMasterIPCommand.getAllErrors()));
				}
			} else {

				po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed("Could not update cluster info in DB! Please see logs\nInstallation aborted");
		}
	}
}
