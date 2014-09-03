package org.safehaus.subutai.impl.solr.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Map;
import java.util.Set;


public class InstallOperationHandler extends AbstractOperationHandler<SolrImpl>
{
	private final Config config;


	public InstallOperationHandler(SolrImpl manager, Config config) {
		super(manager, config.getClusterName());
		this.config = config;
		productOperation = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Installing %s", Config.PRODUCT_KEY));
	}


	@Override
	public void run() {
		if (Strings.isNullOrEmpty(config.getClusterName()) || config.getNumberOfNodes() <= 0) {
			productOperation.addLogFailed("Malformed configuration\nInstallation aborted");
			return;
		}

		if (manager.getCluster(config.getClusterName()) != null) {
			productOperation.addLogFailed(
					String.format("Installation with name '%s' already exists\nInstallation aborted",
							config.getClusterName()));
			return;
		}

		try {
			productOperation.addLog(String.format("Creating %d lxc containers...", config.getNumberOfNodes()));
			Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs(config.getNumberOfNodes());

			for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
				config.getNodes().addAll(entry.getValue());
			}

			productOperation.addLog("Lxc containers created successfully\nInstalling Solr...");


			Command installCommand = manager.getCommands().getInstallCommand(config.getNodes());
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				productOperation.addLog("Installation succeeded\nSaving information to database...");

				try {
					manager.getDbManager().saveInfo2(Config.PRODUCT_KEY, config.getClusterName(), config);

					productOperation.addLogDone("Information saved to database");
				} catch (DBException e) {
					productOperation.addLogFailed(
							String.format("Failed to save information to database, %s", e.getMessage()));

					try {
						manager.getLxcManager().destroyLxcs(lxcAgentsMap);
					} catch (LxcDestroyException ignore) {
					}
				}
			} else {
				productOperation
						.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
			}
		} catch (LxcCreateException ex) {
			productOperation.addLogFailed(ex.getMessage());
		}
	}
}
