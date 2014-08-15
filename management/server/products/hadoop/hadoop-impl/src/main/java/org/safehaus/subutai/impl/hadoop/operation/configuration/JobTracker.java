package org.safehaus.subutai.impl.hadoop.operation.configuration;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.impl.hadoop.Commands;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by daralbaev on 14.04.14.
 */
public class JobTracker {
	public static final int NUMBER_OF_RETRIES = 30;
	public static final int SLEEP_SECONDS = 10;
	private HadoopImpl parent;
	private Config config;

	public JobTracker(HadoopImpl parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public UUID start() {
		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Starting cluster's %s JobTracker", config.getClusterName()));
		final Command command = Commands.getJobTrackerCommand(config.getJobTracker(), "start &");

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getJobTracker().getHostname()));
					return;
				}

				HadoopImpl.getCommandRunner().runCommand(command);

				if (command.hasSucceeded()) {
					final AtomicBoolean isSuccessful = new AtomicBoolean(false);
					for (int i = 1; i <= NUMBER_OF_RETRIES; i++) {

						po.addLog(String.format("Checking status for %d attempt.", i));
						parent.getExecutor().execute(new CheckTask(status(), new CompleteEvent() {
							@Override
							public void onComplete(NodeState state) {
								if (NodeState.RUNNING.equals(state)) {
									po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
									isSuccessful.set(true);
								}
							}
						}));

						if (isSuccessful.get()) {
							break;
						} else {
							try {
								Thread.sleep(SLEEP_SECONDS * 1000);
							} catch (InterruptedException e) {
								break;
							}
						}
					}

					if (!isSuccessful.get()) {
						po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
					}
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
				String.format("Stopping cluster's %s JobTracker", config.getClusterName()));
		final Command command = Commands.getJobTrackerCommand(config.getJobTracker(), "stop &");

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getJobTracker().getHostname()));
					return;
				}

				HadoopImpl.getCommandRunner().runCommand(command);

				if (command.hasSucceeded()) {
					final AtomicBoolean isSuccessful = new AtomicBoolean(false);
					for (int i = 1; i <= NUMBER_OF_RETRIES; i++) {

						po.addLog(String.format("Checking status for %d attempt.", i));
						parent.getExecutor().execute(new CheckTask(status(), new CompleteEvent() {
							@Override
							public void onComplete(NodeState state) {
								if (NodeState.STOPPED.equals(state)) {
									po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
									isSuccessful.set(true);
								}
							}
						}));

						if (isSuccessful.get()) {
							break;
						} else {
							try {
								Thread.sleep(SLEEP_SECONDS * 1000);
							} catch (InterruptedException e) {
								break;
							}
						}
					}

					if (!isSuccessful.get()) {
						po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
					}
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
				String.format("Restarting cluster's %s JobTracker", config.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getJobTracker().getHostname()));
					return;
				}

				final Command command = Commands.getJobTrackerCommand(config.getJobTracker(), "restart &");
				HadoopImpl.getCommandRunner().runCommand(command);

				if (command.hasSucceeded()) {
					final AtomicBoolean isSuccessful = new AtomicBoolean(false);
					for (int i = 1; i <= NUMBER_OF_RETRIES; i++) {

						po.addLog(String.format("Checking status for %d attempt.", i));
						parent.getExecutor().execute(new CheckTask(status(), new CompleteEvent() {
							@Override
							public void onComplete(NodeState state) {
								if (NodeState.RUNNING.equals(state)) {
									po.addLogDone(String.format("Task's operation %s finished", command.getDescription()));
									isSuccessful.set(true);
								}
							}
						}));

						if (isSuccessful.get()) {
							break;
						} else {
							try {
								Thread.sleep(SLEEP_SECONDS * 1000);
							} catch (InterruptedException e) {
								break;
							}
						}
					}

					if (!isSuccessful.get()) {
						po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
					}
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
				= HadoopImpl.getTracker().createProductOperation(String.format("Getting status of clusters %s JobTracker", config.getClusterName()), Config.PRODUCT_KEY);

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getJobTracker().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getJobTracker().getHostname()));
					return;
				}

				Command command = Commands.getJobTrackerCommand(config.getJobTracker(), "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(config.getJobTracker().getUuid());
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
							config.getClusterName(),
							config.getJobTracker().getHostname()
					));
				} else {
					po.addLogDone(String.format("JobTracker of %s is %s",
							config.getJobTracker().getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();
	}
}
