package org.safehaus.subutai.impl.hadoop.operation.configuration;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hadoop.Commands;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;

/**
 * Created by daralbaev on 14.04.14.
 */
public class SecondaryNameNode {
	private HadoopImpl parent;
	private Config config;

	public SecondaryNameNode(HadoopImpl parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public UUID status() {

		final ProductOperation po
				= parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Getting status of clusters %s Secondary NameNode", config.getClusterName()));

		parent.getExecutor().execute(new Runnable() {

			public void run() {
				if (config == null) {
					po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
					return;
				}

				final Agent node = parent.getAgentManager().getAgentByHostname(config.getSecondaryNameNode().getHostname());
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", config.getSecondaryNameNode().getHostname()));
					return;
				}

				Command command = Commands.getNameNodeCommand(config.getSecondaryNameNode(), "status");
				HadoopImpl.getCommandRunner().runCommand(command);

				NodeState nodeState = NodeState.UNKNOWN;
				if (command.hasCompleted()) {
					AgentResult result = command.getResults().get(config.getSecondaryNameNode().getUuid());
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
							config.getClusterName(),
							config.getSecondaryNameNode().getHostname()
					));
				} else {
					po.addLogDone(String.format("Secondary NameNode of %s is %s",
							config.getSecondaryNameNode().getHostname(),
							nodeState
					));
				}
			}
		});

		return po.getId();

	}
}
