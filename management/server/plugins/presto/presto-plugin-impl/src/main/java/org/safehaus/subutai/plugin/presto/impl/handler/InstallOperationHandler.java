package org.safehaus.subutai.plugin.presto.impl.handler;

import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.plugin.presto.api.Config;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dilshat on 5/7/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<PrestoImpl> {
	private final ProductOperation po;
	private final Config config;

	public InstallOperationHandler(PrestoImpl manager, Config config) {
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
		if (Strings.isNullOrEmpty(config.getClusterName()) || Util.isCollectionEmpty(config.getWorkers()) || config.getCoordinatorNode() == null) {
			po.addLogFailed("Malformed configuration\nInstallation aborted");
			return;
		}

		if (manager.getCluster(config.getClusterName()) != null) {
			po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
			return;
		}

		if (manager.getAgentManager().getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
			po.addLogFailed("Coordinator node is not connected\nInstallation aborted");
			return;
		}

		//check if node agent is connected
		for (Iterator<Agent> it = config.getWorkers().iterator(); it.hasNext(); ) {
			Agent node = it.next();
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				po.addLog(String.format("Node %s is not connected. Omitting this node from installation", node.getHostname()));
				it.remove();
			}
		}

		if (config.getWorkers().isEmpty()) {
			po.addLogFailed("No nodes eligible for installation\nInstallation aborted");
			return;
		}

		po.addLog("Checking prerequisites...");

		//check installed ksks packages
		Set<Agent> allNodes = config.getAllNodes();
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(allNodes);
		manager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
			return;
		}
		for (Iterator<Agent> it = allNodes.iterator(); it.hasNext(); ) {
			Agent node = it.next();
			AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());
			if (result.getStdOut().contains("ksks-presto")) {
				po.addLog(String.format("Node %s already has Presto installed. Omitting this node from installation", node.getHostname()));
				config.getWorkers().remove(node);
				it.remove();
			} else if (!result.getStdOut().contains("ksks-hadoop")) {
				po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation", node.getHostname()));
				config.getWorkers().remove(node);
				it.remove();
			}
		}

		if (config.getWorkers().isEmpty()) {
			po.addLogFailed("No nodes eligible for installation\nInstallation aborted");
			return;
		}
		if (!allNodes.contains(config.getCoordinatorNode())) {
			po.addLogFailed("Coordinator node was omitted\nInstallation aborted");
			return;
		}

		po.addLog("Updating db...");
		//save to db
		if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLog("Cluster info saved to DB\nInstalling Presto...");
			//install presto

			Command installCommand = Commands.getInstallCommand(config.getAllNodes());
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				po.addLog("Installation succeeded\nConfiguring coordinator...");

				Command configureCoordinatorCommand = Commands.getSetCoordinatorCommand(config.getCoordinatorNode());
				manager.getCommandRunner().runCommand(configureCoordinatorCommand);

				if (configureCoordinatorCommand.hasSucceeded()) {
					po.addLog("Coordinator configured successfully\nConfiguring workers...");

					Command configureWorkersCommand = Commands.getSetWorkerCommand(config.getCoordinatorNode(), config.getWorkers());
					manager.getCommandRunner().runCommand(configureWorkersCommand);

					if (configureWorkersCommand.hasSucceeded()) {
						po.addLog("Workers configured successfully\nStarting Presto...");

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
							po.addLogDone("Presto started successfully\nDone");
						} else {
							po.addLogFailed(String.format("Failed to start Presto, %s", startNodesCommand.getAllErrors()));
						}

					} else {
						po.addLogFailed(String.format("Failed to configure workers, %s", configureWorkersCommand.getAllErrors()));
					}
				} else {
					po.addLogFailed(String.format("Failed to configure coordinator, %s", configureCoordinatorCommand.getAllErrors()));
				}

			} else {
				po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
		}
	}
}
