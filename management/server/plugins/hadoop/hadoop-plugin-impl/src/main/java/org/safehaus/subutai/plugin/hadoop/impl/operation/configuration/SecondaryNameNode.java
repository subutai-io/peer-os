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

/**
 * Created by daralbaev on 14.04.14.
 */
public class SecondaryNameNode {
	private HadoopImpl parent;
	private HadoopClusterConfig hadoopClusterConfig;

	public SecondaryNameNode(HadoopImpl parent, HadoopClusterConfig hadoopClusterConfig) {
		this.parent = parent;
		this.hadoopClusterConfig = hadoopClusterConfig;
	}

	public UUID status() {

		final ProductOperation po
				= parent.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY,
				String.format("Getting status of clusters %s Secondary NameNode", hadoopClusterConfig.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (hadoopClusterConfig == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted",
							hadoopClusterConfig
									.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(
						hadoopClusterConfig.getSecondaryNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hadoopClusterConfig
							.getSecondaryNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(hadoopClusterConfig.getSecondaryNameNode(), "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(hadoopClusterConfig.getSecondaryNameNode().getUuid());
					if (result.getStdOut() != null && result.getStdOut().contains("SecondaryNameNode")) {
						String[] array = result.getStdOut().split("\n");

						for (String status : array) {
							if (status.contains("SecondaryNameNode")) {
								String temp = status.
										replaceAll("SecondaryNameNode is ", "");
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
							hadoopClusterConfig.getSecondaryNameNode().getHostname()
					));
				} else {
					po.addLogDone(String.format("Secondary NameNode of %s is %s",
							hadoopClusterConfig.getSecondaryNameNode().getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}
}
