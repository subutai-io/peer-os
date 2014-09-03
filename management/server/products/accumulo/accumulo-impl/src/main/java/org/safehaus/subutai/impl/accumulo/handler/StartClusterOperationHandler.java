package org.safehaus.subutai.impl.accumulo.handler;


import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.impl.accumulo.AccumuloImpl;
import org.safehaus.subutai.impl.accumulo.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class StartClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
	private final ProductOperation po;


	public StartClusterOperationHandler(AccumuloImpl manager, String clusterName) {
		super(manager, clusterName);
		po = manager.getTracker()
				.createProductOperation(Config.PRODUCT_KEY, String.format("Starting cluster %s", clusterName));
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

		if (manager.getAgentManager().getAgentByHostname(config.getMasterNode().getHostname()) == null) {
			po.addLogFailed(String.format("Master node '%s' is not connected\nOperation aborted",
					config.getMasterNode().getHostname()));
			return;
		}

		po.addLog("Starting cluster...");

		Command startCommand = Commands.getStartCommand(config.getMasterNode());
		manager.getCommandRunner().runCommand(startCommand);

		//  temporarily turning off until exit code ir fixed:  if ( startCommand.hasSucceeded() ) {
		if (startCommand.hasCompleted()) {
			po.addLogDone("Cluster started successfully");
		} else {
			po.addLogFailed(
					String.format("Failed to start cluster %s, %s", clusterName, startCommand.getAllErrors()));
		}
	}
}
