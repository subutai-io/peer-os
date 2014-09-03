package org.safehaus.subutai.plugin.hive.impl.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hive.impl.CommandType;
import org.safehaus.subutai.plugin.hive.impl.Commands;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
import org.safehaus.subutai.plugin.hive.impl.Product;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;

public class UninstallHandler extends AbstractHandler {

	public UninstallHandler(HiveImpl manager, String clusterName) {
		super(manager, clusterName);
		this.productOperation = manager.getTracker().createProductOperation(
				HiveConfig.PRODUCT_KEY, "Uninstalling cluster " + clusterName);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		HiveConfig config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster '%s' does not exist",
					clusterName));
			return;
		}

		// check server node
		if (!isNodeConnected(config.getServer().getHostname())) {
			po.addLogFailed(String.format("Server node '%s' is not connected",
					config.getServer().getHostname()));
			return;
		}
		// check client nodes
		if (checkClientNodes(config, false) == 0) {
			po.addLogFailed("Connected client(s) not found");
			return;
		}

		po.addLog("Removing Hive client(s)...");
		String s = Commands.make(CommandType.PURGE, Product.HIVE);
		Command cmd = manager.getCommandRunner().createCommand(new RequestBuilder(s),
				config.getClients());
		manager.getCommandRunner().runCommand(cmd);

		if (cmd.hasCompleted())
			for (Agent agent : config.getClients()) {
				AgentResult res = cmd.getResults().get(agent.getUuid());
				if (isZero(res.getExitCode()))
					po.addLog("Hive removed from node " + agent.getHostname());
				else
					po.addLogFailed(String.format("Failed to remove Hive on '%s': %s",
							agent.getHostname(), res.getStdErr()));
			}
		else {
			po.addLogFailed("Failed to remove client(s): " + cmd.getAllErrors());
			return;
		}

		// remove products from server node
		for (Product p : new Product[] {Product.HIVE, Product.DERBY}) {
			s = Commands.make(CommandType.PURGE, p);
			cmd = manager.getCommandRunner().createCommand(new RequestBuilder(s),
					new HashSet<>(Arrays.asList(config.getServer())));
			manager.getCommandRunner().runCommand(cmd);

			if (cmd.hasSucceeded())
				po.addLog(p + " removed from server node");
			else {
				po.addLogFailed("Failed to remove Hive from server");
				return;
			}
		}

		po.addLog("Updating DB...");
		if (manager.getDbManager().deleteInfo(HiveConfig.PRODUCT_KEY, config.getClusterName()))
			po.addLogDone("Cluster info deleted from DB");
		else
			po.addLogFailed("Failed to delete cluster info");

	}

}
