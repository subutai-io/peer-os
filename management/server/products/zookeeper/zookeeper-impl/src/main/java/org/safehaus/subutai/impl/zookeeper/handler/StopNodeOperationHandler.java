package org.safehaus.subutai.impl.zookeeper.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.enums.NodeState;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
	private final ProductOperation po;
	private final String lxcHostname;

	public StopNodeOperationHandler(ZookeeperImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Stopping node %s in %s", lxcHostname, clusterName));
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

		final Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (node == null) {
			po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}
		if (!config.getNodes().contains(node)) {
			po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
			return;
		}
		po.addLog("Stopping node...");

		Command stopCommand = Commands.getStopCommand(node);
		manager.getCommandRunner().runCommand(stopCommand);
		NodeState state = NodeState.UNKNOWN;
		if (stopCommand.hasCompleted()) {
			AgentResult result = stopCommand.getResults().get(node.getUuid());
			if (result.getStdOut().contains("STOPPED")) {
				state = NodeState.STOPPED;
			}
		}

		if (NodeState.STOPPED.equals(state)) {
			po.addLogDone(String.format("Node on %s stopped", lxcHostname));
		} else {
			po.addLogFailed(String.format("Failed to stop node %s. %s",
					lxcHostname, stopCommand.getAllErrors()
			));
		}
	}
}
