package org.safehaus.subutai.impl.accumulo.handler;


import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.impl.accumulo.AccumuloImpl;
import org.safehaus.subutai.impl.accumulo.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;

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
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Checking node %s in %s", lxcHostname, clusterName));
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
			po.addLogFailed(
					String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}
		if (!config.getAllNodes().contains(node)) {
			po.addLogFailed(
					String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
			return;
		}

		Command checkNodeCommand = Commands.getStatusCommand(node);
		manager.getCommandRunner().runCommand(checkNodeCommand);

		//  temporarily turning off until exit code ir fixed:  if ( checkNodeCommand.hasSucceeded() ) {
		if (checkNodeCommand.hasCompleted()) {
			po.addLogDone(String.format("Status on %s is %s", lxcHostname,
					checkNodeCommand.getResults().get(node.getUuid()).getStdOut()));
		} else {
			po.addLogFailed(
					String.format("Failed to check status of %s, %s", lxcHostname, checkNodeCommand.getAllErrors()));
		}
	}
}
