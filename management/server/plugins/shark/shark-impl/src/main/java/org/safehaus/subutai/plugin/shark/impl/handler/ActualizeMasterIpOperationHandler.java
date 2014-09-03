package org.safehaus.subutai.plugin.shark.impl.handler;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import java.util.UUID;

public class ActualizeMasterIpOperationHandler extends AbstractOperationHandler<SharkImpl>
{

	public ActualizeMasterIpOperationHandler(SharkImpl manager, String clusterName) {
		super(manager, clusterName);
		productOperation = manager.getTracker().createProductOperation( SharkClusterConfig.PRODUCT_KEY,
				String.format("Actualizing master IP of %s", clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return productOperation.getId();
	}

	@Override
	public void run() {
		SharkClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			productOperation.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
			return;
		}

		SparkClusterConfig sparkConfig = manager.getSparkManager().getCluster(clusterName);
		if (sparkConfig == null) {
			productOperation.addLogFailed( String.format( "Spark cluster '%s' not found\nInstallation aborted", clusterName ) );
			return;
		}

		for (Agent node : config.getNodes()) {
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				productOperation.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
				return;
			}
		}

		Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getNodes(), sparkConfig.getMasterNode());
		manager.getCommandRunner().runCommand(setMasterIPCommand);

		if (setMasterIPCommand.hasSucceeded()) {
			productOperation.addLogDone( "Master IP actualized successfully\nDone" );
		} else {
			productOperation.addLogFailed( String.format( "Failed to actualize Master IP, %s", setMasterIPCommand.getAllErrors() ) );
		}
	}
}
