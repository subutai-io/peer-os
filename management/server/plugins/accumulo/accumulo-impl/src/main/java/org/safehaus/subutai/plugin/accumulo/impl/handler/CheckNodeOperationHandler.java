package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
	private final String lxcHostname;
	private final ProductOperation po;


	public CheckNodeOperationHandler(AccumuloImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(AccumuloClusterConfig.PRODUCT_KEY,
				String.format("Checking node %s in %s", lxcHostname, clusterName));
	}


	@Override
	public UUID getTrackerId() {
		return po.getId();
	}


	@Override
	public void run() {
		AccumuloClusterConfig accumuloClusterConfig = manager.getCluster(clusterName);
		if (accumuloClusterConfig == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		final Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (node == null) {
			po.addLogFailed(
					String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}
		if (!accumuloClusterConfig.getAllNodes().contains(node)) {
			po.addLogFailed(
					String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
			return;
		}

		Command checkNodeCommand = Commands.getStatusCommand(node);
		manager.getCommandRunner().runCommand(checkNodeCommand);

		if (checkNodeCommand.hasSucceeded()) {
			po.addLogDone(String.format("Status on %s is %s", lxcHostname,
					checkNodeCommand.getResults().get(node.getUuid()).getStdOut()));
		} else {
			po.addLogFailed(
					String.format("Failed to check status of %s, %s", lxcHostname, checkNodeCommand.getAllErrors()));
		}
	}
}
