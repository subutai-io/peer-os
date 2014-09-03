package org.safehaus.subutai.impl.zookeeper.handler;


import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;

import java.util.UUID;


/**
 * Created by dilshat on 5/7/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
	private final ProductOperation po;


	public UninstallOperationHandler(ZookeeperImpl manager, String clusterName) {
		super(manager, clusterName);
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Destroying cluster %s", clusterName));
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

		if (config.isStandalone()) {

			po.addLog("Destroying lxc containers...");

			try {
				manager.getLxcManager().destroyLxcs(config.getNodes());
				po.addLog("Lxc containers successfully destroyed");
			} catch (LxcDestroyException ex) {
				po.addLog(String.format("%s, skipping...", ex.getMessage()));
			}
		} else {
			po.addLog("Uninstalling Zookeeper...");


			Command cmd = Commands.getUninstallCommand(config.getNodes());

			manager.getCommandRunner().runCommand(cmd);

			if (cmd.hasSucceeded()) {
				po.addLog("Uninstalled Zookeeper");
			} else {
				po.addLog(String.format("Failed to uninstall Zookeeper, %s, skipping...", cmd.getAllErrors()));
			}
		}

		po.addLog("Updating information in database...");

		try {
			manager.getDbManager().deleteInfo2(Config.PRODUCT_KEY, clusterName);

			po.addLogDone("Information in database updated successfully");
		} catch (DBException e) {
			po.addLogFailed(String.format("Failed to update information in database, %s", e.getMessage()));
		}
	}
}
