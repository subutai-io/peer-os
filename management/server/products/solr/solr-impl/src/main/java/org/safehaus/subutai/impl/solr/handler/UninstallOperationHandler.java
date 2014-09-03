package org.safehaus.subutai.impl.solr.handler;


import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.solr.SolrImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<SolrImpl>
{

	public UninstallOperationHandler(SolrImpl manager, String clusterName) {
		super(manager, clusterName);
		productOperation = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Destroying installation %s", clusterName));
	}


	@Override
	public void run() {
		Config config = manager.getCluster(clusterName);

		if (config == null) {
			productOperation.addLogFailed(
					String.format("Installation with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		productOperation.addLog("Destroying lxc containers...");

		try {
			manager.getLxcManager().destroyLxcs(config.getNodes());
			productOperation.addLog("Lxc containers successfully destroyed");
		} catch (LxcDestroyException ex) {
			productOperation.addLog(String.format("%s, skipping...", ex.getMessage()));
		}

		productOperation.addLog("Updating db...");

		try {
			manager.getDbManager().deleteInfo2(Config.PRODUCT_KEY, config.getClusterName());
			productOperation.addLogDone("Information updated in database");
		} catch (DBException e) {
			productOperation
					.addLogFailed(String.format("Failed to update information in database, %s", e.getMessage()));
		}
	}
}
