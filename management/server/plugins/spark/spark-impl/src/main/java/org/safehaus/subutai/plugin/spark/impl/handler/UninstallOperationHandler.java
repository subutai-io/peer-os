package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import java.util.UUID;

public class UninstallOperationHandler extends AbstractOperationHandler<SparkImpl> 
{

	public UninstallOperationHandler(SparkImpl manager, String clusterName) {
		super(manager, clusterName);
		productOperation = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
				String.format("Destroying cluster %s", clusterName));
	}

	@Override
	public UUID getTrackerId() {
		return productOperation.getId();
	}

	@Override
	public void run() {
		SparkClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			productOperation.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
			return;
		}

		for (Agent node : config.getAllNodes()) {
			if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
				productOperation.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
				return;
			}
		}

		productOperation.addLog( "Uninstalling Spark..." );

		Command uninstallCommand = Commands.getUninstallCommand(config.getAllNodes());
		manager.getCommandRunner().runCommand(uninstallCommand);

		if (uninstallCommand.hasCompleted()) {
			for (AgentResult result : uninstallCommand.getResults().values()) {
				Agent agent = manager.getAgentManager().getAgentByUUID(result.getAgentUUID());
				if (result.getExitCode() != null && result.getExitCode() == 0) {
					if (result.getStdOut().contains("Package ksks-spark is not installed, so not removed")) {
						productOperation.addLog( String.format( "Spark is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
					} else {
						productOperation.addLog( String.format( "Spark is removed from node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
					}
				} else {
					productOperation.addLog( String.format( "Error %s on node %s", result.getStdErr(),
                            agent == null ? result.getAgentUUID() : agent.getHostname() ) );
				}
			}
			productOperation.addLog( "Updating db..." );
			if (manager.getDbManager().deleteInfo(SparkClusterConfig.PRODUCT_KEY, config.getClusterName())) {
				productOperation.addLogDone( "Cluster info deleted from DB\nDone" );
			} else {
				productOperation.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
			}
		} else {
			productOperation.addLogFailed( String.format( "Uninstallation failed, %s", uninstallCommand.getAllErrors() ) );
		}
	}
}
