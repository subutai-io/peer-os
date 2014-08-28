package org.safehaus.subutai.impl.solr.handler;


import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
	private final String lxcHostname;


	public DestroyNodeOperationHandler(SolrImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		productOperation = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Destroying %s in %s", lxcHostname, clusterName));
	}


	@Override
	public void run() {
		Config config = manager.getCluster(clusterName);

		if (config == null) {
			productOperation.addLogFailed(
					String.format("Installation with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);

		if (agent == null) {
			productOperation.addLogFailed(
					String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}

		if (!config.getNodes().contains(agent)) {
			productOperation.addLogFailed(
					String.format("Agent with hostname %s does not belong to installation %s", lxcHostname,
							clusterName));
			return;
		}


		// Destroy lxc
		productOperation.addLog("Destroying lxc container...");
		Agent physicalAgent = manager.getAgentManager().getAgentByHostname(agent.getParentHostName());

		if (physicalAgent == null) {
			productOperation.addLog(
					String.format("Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
							agent.getHostname()));
		} else {
			if (!manager.getLxcManager().destroyLxcOnHost(physicalAgent, agent.getHostname())) {
				productOperation.addLog("Could not destroy lxc container. Use LXC module to cleanup, skipping...");
			} else {
				productOperation.addLog("Lxc container destroyed successfully");
			}
		}

		// Update db
		productOperation.addLog("Saving information to database...");

		try {
			manager.getDbManager().deleteInfo2(Config.PRODUCT_KEY, config.getClusterName());
			productOperation.addLogDone("Saved information to database");
		} catch (DBException e) {
			productOperation
					.addLogFailed(String.format("Failed to save infomation to database, %s", e.getMessage()));
		}
	}
}
