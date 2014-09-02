package org.safehaus.subutai.impl.hadoop.operation.configuration;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hadoop.Commands;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;

/**
 * Created by daralbaev on 15.04.14.
 */
public class TaskTracker {
	private HadoopImpl parent;
	private Config config;

	public TaskTracker(HadoopImpl parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public UUID status(final Agent agent) {

		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Getting status of clusters %s TaskTracker", agent.getHostname()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {

				final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
					return;
				}

				Command command = Commands.getJobTrackerCommand(agent, "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(agent.getUuid());
					if (result.getStdOut() != null && result.getStdOut().contains("TaskTracker")) {
						String[] array = result.getStdOut().split("\n");

						for (String status : array) {
							if (status.contains("TaskTracker")) {
								String temp = status.
										replaceAll("TaskTracker is ", "");
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
					po.addLogFailed(String.format("Failed to check status of %s",
							agent.getHostname()
					));
				} else {
					po.addLogDone(String.format("DataNode of %s is %s",
							agent.getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}

	public UUID block(final Agent agent) {

		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Blocking TaskTracker of %s cluster", agent.getHostname()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {

				final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
					return;
				}

				Command command = Commands.getRemoveTaskTrackerCommand(config, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getIncludeTaskTrackerCommand(config, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getRefreshJobTrackerCommand(config);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				config.getBlockedAgents().add(agent);
				if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
					po.addLog("Cluster info saved to DB");
				} else {
					po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
							"Blocking node aborted");
				}
			}
		});

		return po.getId();

	}

	private void logCommand(Command command, ProductOperation po) {
		if (command.hasSucceeded()) {
			po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
		} else if (command.hasCompleted()) {
			po.addLogFailed(String.format("Task's operation %s failed", command.getDescription()));
		} else {
			po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
		}
	}

	public UUID unblock(final Agent agent) {

		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Unblocking TaskTracker of %s cluster", agent.getHostname()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {

				final Agent node = parent.getAgentManager().getAgentByHostname(agent.getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
					return;
				}

				Command command = Commands.getSetTaskTrackerCommand(config, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getExcludeTaskTrackerCommand(config, agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				command = Commands.getStartTaskTrackerCommand(agent);
				HadoopImpl.getCommandRunner().runCommand(command);
				logCommand(command, po);

				config.getBlockedAgents().remove(agent);
				if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
					po.addLog("Cluster info saved to DB");
				} else {
					po.addLogFailed("Could not save cluster info to DB! Please see logs\n" +
							"Blocking node aborted");
				}
			}
		});

		return po.getId();

	}
}
