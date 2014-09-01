package org.safehaus.subutai.impl.spark.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.impl.spark.Commands;
import org.safehaus.subutai.impl.spark.SparkImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
	private final ProductOperation po;
	private final String lxcHostname;

	public CheckNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Checking state of %s in %s", lxcHostname, clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return po.getId();
	}

	@Override
	public void run() {
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
			return;
		}

		Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (node == null) {
			po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
			return;
		}

		if (!config.getAllNodes().contains(node)) {
			po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
			return;
		}

		po.addLog("Checking node...");

		Command checkNodeCommand = Commands.getStatusAllCommand(node);
		manager.getCommandRunner().runCommand(checkNodeCommand);

		AgentResult res = checkNodeCommand.getResults().get(node.getUuid());
		if (checkNodeCommand.hasSucceeded()) {
			po.addLogDone(String.format("%s", res.getStdOut()));
		} else {
			po.addLogFailed(String.format("Faied to check status, %s", checkNodeCommand.getAllErrors()));
		}
	}
}
