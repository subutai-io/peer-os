package org.safehaus.subutai.plugin.hadoop.impl.operation.configuration;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.Commands;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by daralbaev on 12.04.14.
 */
public class NameNode {
	private HadoopImpl parent;
	private HadoopClusterConfig hadoopClusterConfig;

	public NameNode(HadoopImpl parent, HadoopClusterConfig hadoopClusterConfig) {
		this.parent = parent;
		this.hadoopClusterConfig = hadoopClusterConfig;
	}

	public UUID start() {
		final ProductOperation po
				= parent.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Starting cluster's %s NameNode", hadoopClusterConfig.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted",
							hadoopClusterConfig
									.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(hadoopClusterConfig.getNameNode(), "start");
				HadoopImpl.getCommandRunner().runCommand(command);

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
				= parent.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Stopping cluster's %s NameNode", hadoopClusterConfig.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", hadoopClusterConfig
							.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(hadoopClusterConfig.getNameNode(), "stop");
				HadoopImpl.getCommandRunner().runCommand(command);

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
				= parent.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Restarting cluster's %s NameNode", hadoopClusterConfig.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", hadoopClusterConfig
							.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(hadoopClusterConfig.getNameNode(), "restart");
				HadoopImpl.getCommandRunner().runCommand(command);

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
				= parent.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Getting status of clusters %s NameNode", hadoopClusterConfig.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", hadoopClusterConfig
							.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(hadoopClusterConfig.getNameNode(), "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(hadoopClusterConfig.getNameNode().getUuid());
					if (result.getStdOut() != null && result.getStdOut().contains("NameNode")) {
						String[] array = result.getStdOut().split("\n");

						for (String status : array) {
							if (status.contains("NameNode")) {
								String temp = status.
										replaceAll(Pattern.quote("!(SecondaryNameNode is not running on this machine)" +
												""), "").
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
							hadoopClusterConfig.getClusterName(),
							hadoopClusterConfig.getNameNode().getHostname()
					));
				} else {
					po.addLogDone(String.format("NameNode of %s is %s",
							hadoopClusterConfig.getNameNode().getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}
}
