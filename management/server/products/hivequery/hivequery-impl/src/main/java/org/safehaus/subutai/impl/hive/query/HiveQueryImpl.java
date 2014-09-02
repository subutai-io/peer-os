package org.safehaus.subutai.impl.hive.query;

import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.hive.query.Config;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;

import java.util.List;
import java.util.UUID;

public class HiveQueryImpl extends HiveQueryBase {

	@Override
	public boolean save(Config config) {
		return dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config);
	}

	@Override
	public boolean save(String name, String query, String description) {
		return dbManager.saveInfo(Config.PRODUCT_KEY, name, new Config(name, query, description));
	}

	@Override
	public UUID run(final String hostname, final String query) {
		final ProductOperation po
				= getTracker().createProductOperation(Config.PRODUCT_KEY,
				"Running query in Hive");

		getExecutor().execute(new Runnable() {

			public void run() {
				if (Strings.isNullOrEmpty(query)) {
					po.addLogFailed("Query does not exist\nOperation aborted");
					return;
				}

				final Agent node = getAgentManager().getAgentByHostname(hostname);
				if (node == null) {
					po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hostname));
					return;
				}

				Command command = Commands.geRunCommand(node, query);
				getCommandRunner().runCommand(command);

				if (command.hasSucceeded()) {
					AgentResult result = command.getResults().get(node.getUuid());
					if (result.getStdOut() != null) {
						po.addLogDone(String.format("%s\n\n%s", result.getStdOut(), result.getStdErr()));
					}
				} else if (command.hasCompleted()) {
					po.addLogFailed(String.format("Task's operation %s failed", command.getDescription()));
				} else {
					po.addLogFailed(String.format("Task's operation %s timeout", command.getDescription()));
				}

			}
		});

		return po.getId();
	}

	@Override
	public List<Config> load() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public List<org.safehaus.subutai.api.hadoop.Config> getHadoopClusters() {
		return dbManager.getInfo(
				org.safehaus.subutai.api.hadoop.Config.PRODUCT_KEY,
				org.safehaus.subutai.api.hadoop.Config.class);
	}


	@Override
	public UUID installCluster(Config config) {
		return null;
	}

	@Override
	public UUID uninstallCluster(String clusterName) {
		return null;
	}

	@Override
	public List<Config> getClusters() {
		return null;
	}

	@Override
	public Config getCluster(String clusterName) {
		return null;
	}


}
