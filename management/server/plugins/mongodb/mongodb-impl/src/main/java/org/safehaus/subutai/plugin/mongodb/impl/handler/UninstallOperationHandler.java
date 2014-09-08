package org.safehaus.subutai.plugin.mongodb.impl.handler;


import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;

import java.util.UUID;


/**
 * Handles uninstall mongo cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<MongoImpl> {
	private final ProductOperation po;


	public UninstallOperationHandler(MongoImpl manager, String clusterName) {
		super(manager, clusterName);
		po = manager.getTracker().createProductOperation(MongoClusterConfig.PRODUCT_KEY,
				String.format("Destroying cluster %s", clusterName));
	}


	@Override
	public UUID getTrackerId() {
		return po.getId();
	}


	@Override
	public void run() {
		MongoClusterConfig config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
			return;
		}

		po.addLog("Destroying lxc containers");
		try {
			manager.getContainerManager().clonesDestroy(config.getAllNodes());
			po.addLog("Lxc containers successfully destroyed");
		} catch (LxcDestroyException ex) {
			po.addLog(String.format("%s, skipping...", ex.getMessage()));
		}

		po.addLog("Deleting cluster information from database..");

		try {
			manager.getPluginDAO().deleteInfo(MongoClusterConfig.PRODUCT_KEY, config.getClusterName());
			po.addLogDone("Cluster info deleted from database");
		} catch (DBException e) {
			po.addLogFailed(String.format("Error while deleting cluster info from database, %s", e.getMessage()));
		}
	}
}
