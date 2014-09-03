package org.safehaus.subutai.impl.hive.handler;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hive.CommandType;
import org.safehaus.subutai.impl.hive.Commands;
import org.safehaus.subutai.impl.hive.HiveImpl;
import org.safehaus.subutai.impl.hive.Product;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddNodeHandler extends AbstractHandler {

	private final String hostname;

	public AddNodeHandler(HiveImpl manager, String clusterName, String hostname) {
		super(manager, clusterName);
		this.hostname = hostname;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY, "Add node to cluster: " + hostname);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
		if (agent == null) {
			po.addLogFailed(String.format("Node '%s' is not connected", hostname));
			return;
		}

		String s = Commands.make(CommandType.LIST, null);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s), new HashSet<>(Arrays.asList(agent)));
		manager.getCommandRunner().runCommand(cmd);

		if (!cmd.hasSucceeded()) {
			po.addLogFailed("Failed to check installed packages");
			return;
		}
		AgentResult res = cmd.getResults().get(agent.getUuid());
		boolean skipInstall;
		if (skipInstall = res.getStdOut().contains(Product.HIVE.getPackageName()))
			po.addLog("Hive already installed on " + hostname);

		config.getClients().add(agent);

		po.addLog("Update cluster info...");
		if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLog("Cluster info updated");

			Set<Agent> set = new HashSet<>(2);
			set.add(agent);
			boolean installed = false;

			if (!skipInstall) {
				s = Commands.make(CommandType.INSTALL, Product.HIVE);
				cmd = manager.getCommandRunner().createCommand(
						new RequestBuilder(s).withTimeout(120), set);
				manager.getCommandRunner().runCommand(cmd);
				installed = cmd.hasSucceeded();
				if (installed)
					po.addLog(String.format("Hive successfully installed on '%s'",
							hostname));
				else
					po.addLog("Failed to add node: " + cmd.getAllErrors());
			}

			if (skipInstall || installed) {
				// configure client
				s = Commands.configureClient(config.getServer());
				cmd = manager.getCommandRunner().createCommand(
						new RequestBuilder(s), set);
				manager.getCommandRunner().runCommand(cmd);

				res = cmd.getResults().get(agent.getUuid());
				installed = cmd.hasSucceeded();
				if (installed)
					po.addLog("Hive client successfully configured");
				else {
					po.addLog(res.getStdOut());
					po.addLog(res.getStdErr());
				}
			}

			if (installed) po.addLogDone("Done");
			else po.addLogFailed(null);

		} else
			po.addLogFailed("Failed to update cluster info");

	}

}
