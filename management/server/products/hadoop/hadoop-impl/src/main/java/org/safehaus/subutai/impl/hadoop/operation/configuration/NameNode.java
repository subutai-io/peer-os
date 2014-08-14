package org.safehaus.subutai.impl.hadoop.operation.configuration;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.impl.hadoop.Commands;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by daralbaev on 12.04.14.
 */
public class NameNode {
	private HadoopImpl parent;
	private Config config;

	public NameNode(HadoopImpl parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public UUID start() {
		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Starting cluster's %s NameNode", config.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(config.getNameNode(), "start &");
				HadoopImpl.getCommandRunner().runCommand(command);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}

				if (command.hasSucceeded()) {
					po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
				} else if (command.hasCompleted()) {
					po.addLogFailed(String.format("Task's operation %s failed", command.getDescription()));
				} else {
					po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
				}
			}
		});

		return po.getId();
	}

	public UUID stop() {

		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Stopping cluster's %s NameNode", config.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(config.getNameNode(), "stop &");
				HadoopImpl.getCommandRunner().runCommand(command);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}

				if (command.hasSucceeded()) {
					po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
				} else if (command.hasCompleted()) {
					po.addLogFailed(String.format("Task's operation %s failed", command.getDescription()));
				} else {
					po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
				}
			}
		});

		return po.getId();
	}

	public UUID restart() {
		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Restarting cluster's %s NameNode", config.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(config.getNameNode(), "restart &");
				HadoopImpl.getCommandRunner().runCommand(command);

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}

				if (command.hasSucceeded()) {
					po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
				} else if (command.hasCompleted()) {
					po.addLogFailed(String.format("Task's operation %s failed", command.getDescription()));
				} else {
					po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
				}
			}
		});

		return po.getId();
	}

	public UUID status() {

		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Getting status of clusters %s NameNode", config.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(config.getNameNode(), "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(config.getNameNode().getUuid());
					if (result.getStdOut() != null && result.getStdOut().contains("NameNode")) {
						String[] array = result.getStdOut().split("\n");

						for (String status : array) {
							if (status.contains("NameNode")) {
								String temp = status.
										replaceAll(Pattern.quote("!(SecondaryNameNode is not running on this machine)"), "").
										replaceAll("NameNode is ", "");
								if (temp.toLowerCase().contains("not")) {
									nodeState = NodeState.STOPPED;
								} else {
									nodeState = NodeState.RUNNING;
								}
							}
						}
					}
				}

				if (NodeState.UNKNOWN.equals(nodeState)) {
					po.addLogFailed(String.format("Failed to check status of %s, %s",
							config.getClusterName(),
							config.getNameNode().getHostname()
					));
				} else {
					po.addLogDone(String.format("NameNode of %s is %s",
							config.getNameNode().getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}
}
