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

public class JobTracker {
    private HadoopClusterConfig hadoopClusterConfig;

	public JobTracker( HadoopClusterConfig hadoopClusterConfig ) {
        this.hadoopClusterConfig = hadoopClusterConfig;
	}

	public UUID start() {
		final ProductOperation po
				= HadoopImpl.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Starting cluster's %s JobTracker", hadoopClusterConfig.getClusterName()));

		HadoopImpl.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted",
							hadoopClusterConfig
									.getClusterName()));
					return;
				}

				final Agent node = HadoopImpl.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getJobTracker().getHostname()));
					return;
				}

				Command command = Commands.getJobTrackerCommand(hadoopClusterConfig.getJobTracker(), "start");
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
				= HadoopImpl.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Stopping cluster's %s JobTracker", hadoopClusterConfig.getClusterName()));

		HadoopImpl.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", hadoopClusterConfig
							.getClusterName()));
					return;
				}

				final Agent node = HadoopImpl.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getJobTracker().getHostname()));
					return;
				}

				Command command = Commands.getJobTrackerCommand(hadoopClusterConfig.getJobTracker(), "stop");
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
				= HadoopImpl.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Restarting cluster's %s JobTracker", hadoopClusterConfig.getClusterName()));

		HadoopImpl.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", hadoopClusterConfig
							.getClusterName()));
					return;
				}

				final Agent node = HadoopImpl.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getJobTracker().getHostname()));
					return;
				}

				Command command = Commands.getJobTrackerCommand(hadoopClusterConfig.getJobTracker(), "restart");
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
				= HadoopImpl.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Getting status of clusters %s JobTracker", hadoopClusterConfig.getClusterName()));

		HadoopImpl.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", hadoopClusterConfig
							.getClusterName()));
					return;
				}

				final Agent node = HadoopImpl.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getJobTracker().getHostname()));
					return;
				}

				Command command = Commands.getJobTrackerCommand(hadoopClusterConfig.getJobTracker(), "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(hadoopClusterConfig.getJobTracker().getUuid());
					if (result.getStdOut() != null && result.getStdOut().contains("JobTracker")) {
						String[] array = result.getStdOut().split("\n");

						for (String status : array) {
							if (status.contains("JobTracker")) {
								String temp = status.
										replaceAll("DataNode is ", "");
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
							hadoopClusterConfig.getJobTracker().getHostname()
					));
				} else {
					po.addLogDone(String.format("JobTracker of %s is %s",
							hadoopClusterConfig.getJobTracker().getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}
}
