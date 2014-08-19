package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
	private final ProductOperation po;


	public UninstallOperationHandler(AccumuloImpl manager, String clusterName) {
		super(manager, clusterName);
		po = manager.getTracker().createProductOperation(AccumuloClusterConfig.PRODUCT_KEY,
				String.format("Uninstalling cluster %s", clusterName));
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

		po.addLog("Uninstalling cluster...");

		Command uninstallCommand = Commands.getUninstallCommand(accumuloClusterConfig.getAllNodes());
		manager.getCommandRunner().runCommand(uninstallCommand);

		if (uninstallCommand.hasCompleted()) {
			if (uninstallCommand.hasSucceeded()) {
				po.addLog("Cluster successfully uninstalled");
			} else {
				po.addLog(String.format("Uninstallation failed, %s, skipping...", uninstallCommand.getAllErrors()));
			}

			po.addLog("Removing Accumulo from HDFS...");

			Command removeAccumuloFromHDFSCommand = Commands.getRemoveAccumuloFromHFDSCommand(accumuloClusterConfig.getMasterNode());
			manager.getCommandRunner().runCommand(removeAccumuloFromHDFSCommand);

			if (removeAccumuloFromHDFSCommand.hasSucceeded()) {
				po.addLog("Accumulo successfully removed from HDFS");
			} else {
				po.addLog(String.format("Removing Accumulo from HDFS failed, %s, skipping...",
						removeAccumuloFromHDFSCommand.getAllErrors()));
			}

			po.addLog("Updating db...");
			if (manager.getDbManager().deleteInfo(AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName())) {
				po.addLogDone("Cluster info deleted from DB\nDone");
			} else {
				po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
			}
		} else {
			po.addLogFailed("Uninstallation failed, command timed out");
		}
	}
}
