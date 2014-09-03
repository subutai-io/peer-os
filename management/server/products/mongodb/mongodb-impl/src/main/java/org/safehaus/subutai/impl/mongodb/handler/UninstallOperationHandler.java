package org.safehaus.subutai.impl.mongodb.handler;


import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.impl.mongodb.MongoImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<MongoImpl> {
	private final ProductOperation po;


	public UninstallOperationHandler(MongoImpl manager, String clusterName) {
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
			po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
			return;
		}

		po.addLog("Destroying lxc containers");
		try {
			manager.getLxcManager().destroyLxcs(config.getAllNodes());
			po.addLog("Lxc containers successfully destroyed");
		} catch (LxcDestroyException ex) {
			po.addLog(String.format("%s, skipping...", ex.getMessage()));
		}

		po.addLog("Updating db...");

		try {
			manager.getDbManager().deleteInfo2(Config.PRODUCT_KEY, config.getClusterName());

			po.addLogDone("Database information updated");
		} catch (DBException e) {
			po.addLogFailed(String.format("Failed to update database information, %s", e.getMessage()));
		}
	}
}
