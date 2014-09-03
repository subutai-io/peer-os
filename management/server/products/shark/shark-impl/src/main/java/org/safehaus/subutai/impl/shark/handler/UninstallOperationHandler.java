package org.safehaus.subutai.impl.shark.handler;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.shark.Commands;
import org.safehaus.subutai.impl.shark.SharkImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<SharkImpl>
{
	private final ProductOperation po;

	public UninstallOperationHandler(SharkImpl manager, String clusterName) {
		super(manager, clusterName);
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Destroying cluster %s", clusterName));
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

		for (Agent node : config.getNodes()) {
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
				return;
			}
		}

		po.addLog("Uninstalling Shark...");

		Command uninstallCommand = Commands.getUninstallCommand(config.getNodes());
		manager.getCommandRunner().runCommand(uninstallCommand);

		if (uninstallCommand.hasCompleted()) {
			for (AgentResult result : uninstallCommand.getResults().values()) {
				Agent agent = manager.getAgentManager().getAgentByUUID(result.getAgentUUID());
				if (result.getExitCode() != null && result.getExitCode() == 0) {
					if (result.getStdOut().contains("Package ksks-shark is not installed, so not removed")) {
						po.addLog(String.format("Shark is not installed, so not removed on node %s",
								agent == null ? result.getAgentUUID() : agent.getHostname()));
					} else {
						po.addLog(String.format("Shark is removed from node %s",
								agent == null ? result.getAgentUUID() : agent.getHostname()));
					}
				} else {
					po.addLog(String.format("Error %s on node %s", result.getStdErr(),
							agent == null ? result.getAgentUUID() : agent.getHostname()));
				}
			}
			po.addLog("Updating db...");
			if (manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
				po.addLogDone("Cluster info deleted from DB\nDone");
			} else {
				po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
			}
		} else {
			po.addLogFailed("Uninstallation failed, command timed out");
		}
	}
}
