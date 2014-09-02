package org.safehaus.subutai.impl.shark.handler;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.shark.Commands;
import org.safehaus.subutai.impl.shark.SharkImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class ActualizeMasterIpOperationHandler extends AbstractOperationHandler<SharkImpl>
{
	private final ProductOperation po;

	public ActualizeMasterIpOperationHandler(SharkImpl manager, String clusterName) {
		super(manager, clusterName);
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Actualizing master IP of %s", clusterName));
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

		org.safehaus.subutai.api.spark.Config sparkConfig
				= manager.getSparkManager().getCluster(clusterName);
		if (sparkConfig == null) {
			po.addLogFailed(String.format("Spark cluster '%s' not found\nInstallation aborted", clusterName));
			return;
		}

		for (Agent node : config.getNodes()) {
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
				return;
			}
		}

		Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getNodes(), sparkConfig.getMasterNode());
		manager.getCommandRunner().runCommand(setMasterIPCommand);

		if (setMasterIPCommand.hasSucceeded()) {
			po.addLogDone("Master IP actualized successfully\nDone");
		} else {
			po.addLogFailed(String.format("Failed to actualize Master IP, %s", setMasterIPCommand.getAllErrors()));
		}
	}
}
