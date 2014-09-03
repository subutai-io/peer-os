package org.safehaus.subutai.plugin.presto.impl.handler;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandCallback;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class ChangeCoordinatorNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {
	private final ProductOperation po;
	private final String newCoordinatorHostname;

	public ChangeCoordinatorNodeOperationHandler(PrestoImpl manager, String clusterName, String newCoordinatorHostname) {
		super(manager, clusterName);
		this.newCoordinatorHostname = newCoordinatorHostname;
		po = manager.getTracker().createProductOperation(PrestoClusterConfig.PRODUCT_KEY,
				String.format("Changing coordinator to %s in %s", newCoordinatorHostname, clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
        productOperation = po;
		final PrestoClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		if (manager.getAgentManager().getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
			po.addLogFailed(String.format("Coordinator %s is not connected\nOperation aborted", config.getCoordinatorNode().getHostname()));
			return;
		}

		Agent newCoordinator = manager.getAgentManager().getAgentByHostname(newCoordinatorHostname);
		if (newCoordinator == null) {
			po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", newCoordinatorHostname));
			return;
		}

		if (newCoordinator.equals(config.getCoordinatorNode())) {
			po.addLogFailed(String.format("Node %s is already a coordinator node\nOperation aborted", newCoordinatorHostname));
			return;
		}

		//check if node is in the cluster
		if (!config.getWorkers().contains(newCoordinator)) {
			po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", newCoordinatorHostname));
			return;
		}

		po.addLog("Stopping all nodes...");
		//stop all nodes
		Command stopNodesCommand = Commands.getStopCommand(config.getAllNodes());
		manager.getCommandRunner().runCommand(stopNodesCommand);

		if (stopNodesCommand.hasSucceeded()) {
			po.addLog("All nodes stopped\nConfiguring coordinator...");

			Command configureCoordinatorCommand = Commands.getSetCoordinatorCommand(newCoordinator);
			manager.getCommandRunner().runCommand(configureCoordinatorCommand);

			if (configureCoordinatorCommand.hasSucceeded()) {
				po.addLog("Coordinator configured successfully");
			} else {
				po.addLogFailed(String.format("Failed to configure coordinator, %s\nOperation aborted", configureCoordinatorCommand.getAllErrors()));
				return;
			}

			config.getWorkers().add(config.getCoordinatorNode());
			config.getWorkers().remove(newCoordinator);
			config.setCoordinatorNode(newCoordinator);

			po.addLog("Configuring workers...");

			Command configureWorkersCommand = Commands.getSetWorkerCommand(newCoordinator, config.getWorkers());
			manager.getCommandRunner().runCommand(configureWorkersCommand);

			if (configureWorkersCommand.hasSucceeded()) {
				po.addLog("Workers configured successfully\nStarting cluster...");

				Command startNodesCommand = Commands.getStartCommand(config.getAllNodes());
				final AtomicInteger okCount = new AtomicInteger();
				manager.getCommandRunner().runCommand(startNodesCommand, new CommandCallback() {

					@Override
					public void onResponse(Response response, AgentResult agentResult, Command command) {
						if (agentResult.getStdOut().contains("Started") && okCount.incrementAndGet() == config.getAllNodes().size()) {
							stop();
						}
					}

				});

				if (okCount.get() == config.getAllNodes().size()) {
					po.addLog("Cluster started successfully");
				} else {
					po.addLog(String.format("Start of cluster failed, %s, skipping...", startNodesCommand.getAllErrors()));
				}

				po.addLog("Updating db...");
				//update db
				if (manager.getDbManager().saveInfo(PrestoClusterConfig.PRODUCT_KEY, clusterName, config)) {
					po.addLogDone("Cluster info updated in DB\nDone");
				} else {
					po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
				}
			} else {
				po.addLogFailed(String.format("Failed to configure workers, %s\nOperation aborted", configureWorkersCommand.getAllErrors()));
			}

		} else {
			po.addLogFailed(String.format("Failed to stop all nodes, %s", stopNodesCommand.getAllErrors()));
		}
	}
}
