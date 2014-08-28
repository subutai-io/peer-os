package org.safehaus.subutai.plugin.mahout.impl.handler;

import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.mahout.api.MahoutConfig;
import org.safehaus.subutai.plugin.mahout.impl.Commands;
import org.safehaus.subutai.plugin.mahout.impl.MahoutImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.Iterator;
import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<MahoutImpl> {
	private final ProductOperation po;
	private final MahoutConfig config;

	public InstallOperationHandler(MahoutImpl manager, MahoutConfig config) {
		super(manager, config.getClusterName());
		this.config = config;
		po = manager.getTracker().createProductOperation( MahoutConfig.PRODUCT_KEY,
				String.format("Installing %s", MahoutConfig.PRODUCT_KEY));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
		if (Strings.isNullOrEmpty(config.getClusterName()) || Util.isCollectionEmpty(config.getNodes())) {
			po.addLogFailed("Malformed configuration\nInstallation aborted");
			return;
		}

		if (manager.getCluster(config.getClusterName()) != null) {
			po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
			return;
		}

		//check if node agent is connected
		for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
			Agent node = it.next();
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				po.addLog(String.format("Node %s is not connected. Omitting this node from installation", node.getHostname()));
				it.remove();
			}
		}

		if (config.getNodes().isEmpty()) {
			po.addLogFailed("No nodes eligible for installation. Operation aborted");
			return;
		}

		po.addLog("Checking prerequisites...");

		//check installed ksks packages
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getNodes());
		manager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
			return;
		}

		for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
			Agent node = it.next();

			AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

			if (result.getStdOut().contains("ksks-mahout")) {
				po.addLog(String.format("Node %s already has Mahout installed. Omitting this node from installation", node.getHostname()));
				it.remove();
			} else if (!result.getStdOut().contains("ksks-hadoop")) {
				po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation", node.getHostname()));
				it.remove();
			}
		}

		if (config.getNodes().isEmpty()) {
			po.addLogFailed("No nodes eligible for installation. Operation aborted");
			return;
		}
		po.addLog("Updating db...");
		//save to db
		if (manager.getDbManager().saveInfo( MahoutConfig.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLog("Cluster info saved to DB\nInstalling Mahout...");

			//install mahout
			Command installCommand = Commands.getInstallCommand(config.getNodes());
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				po.addLogDone("Installation succeeded\nDone");
			} else {
				po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
		}
	}
}
