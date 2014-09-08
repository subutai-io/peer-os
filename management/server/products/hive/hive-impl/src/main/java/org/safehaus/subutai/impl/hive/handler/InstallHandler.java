package org.safehaus.subutai.impl.hive.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hive.CommandType;
import org.safehaus.subutai.impl.hive.Commands;
import org.safehaus.subutai.impl.hive.HiveImpl;
import org.safehaus.subutai.impl.hive.Product;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.*;

public class InstallHandler extends AbstractHandler {

	private final Config config;

	public InstallHandler(HiveImpl manager, Config config) {
		super(manager, config.getClusterName());
		this.config = config;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY,
				"Installing cluster " + config.getClusterName());
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		if (manager.getCluster(clusterName) != null) {
			po.addLogFailed(String.format("Cluster '%s' already exists",
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
		if (checkClientNodes(config, true) == 0) {
			po.addLogFailed("No nodes eligible for installation. Operation aborted");
			return;
		}

		po.addLog("Check installed packages...");
		// server packages
		String s = Commands.make(CommandType.LIST, null);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s),
				new HashSet<>(Arrays.asList(config.getServer())));
		manager.getCommandRunner().runCommand(cmd);

		if (!cmd.hasCompleted()) {
			po.addLogFailed("Failed to check installed packages for server node");
			return;
		}
		AgentResult res = cmd.getResults().get(config.getServer().getUuid());
		boolean skipHive = res.getStdOut().contains(Product.HIVE.getPackageName());
		boolean skipDerby = res.getStdOut().contains(Product.DERBY.getPackageName());

		// check clients
		s = Commands.make(CommandType.LIST, null);
		cmd = manager.getCommandRunner().createCommand(new RequestBuilder(s),
				config.getClients());
		manager.getCommandRunner().runCommand(cmd);

		if (!cmd.hasCompleted()) {
			po.addLogFailed("Failed to check installed packages");
			return;
		}
		Iterator<Agent> it = config.getClients().iterator();
		while (it.hasNext()) {
			Agent a = it.next();
			res = cmd.getResults().get(a.getUuid());
			if (res.getStdOut().contains(Product.HIVE.getPackageName())) {
				po.addLog(String.format("Node '%s' has already Hive installed.\nOmitting from installation",
						a.getHostname()));
				it.remove();
			}
		}
		if (config.getClients().isEmpty()) {
			po.addLogFailed("No client nodes eligible for installation. Operation aborted");
			return;
		}

		// save cluster info and install
		po.addLog("Save cluster info");
		if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLog("Cluster info saved");

			po.addLog("Installing server...");
			if (!skipHive) {
				s = Commands.make(CommandType.INSTALL, Product.HIVE);
				cmd = manager.getCommandRunner().createCommand(
						new RequestBuilder(s).withTimeout(120),
						new HashSet<>(Arrays.asList(config.getServer())));
				manager.getCommandRunner().runCommand(cmd);
				if (!cmd.hasSucceeded()) {
					po.addLogFailed(cmd.getAllErrors());
					return;
				}
			}
			if (!skipDerby) {
				s = Commands.make(CommandType.INSTALL, Product.DERBY);
				cmd = manager.getCommandRunner().createCommand(
						new RequestBuilder(s).withTimeout(120),
						new HashSet<>(Arrays.asList(config.getServer())));
				manager.getCommandRunner().runCommand(cmd);
				if (!cmd.hasSucceeded()) {
					po.addLogFailed(cmd.getAllErrors());
					return;
				}
			}
			// configure Hive server
			s = Commands.configureHiveServer(config.getServer().getListIP().get(0));
			cmd = manager.getCommandRunner().createCommand(new RequestBuilder(s),
					new HashSet<>(Arrays.asList(config.getServer())));
			manager.getCommandRunner().runCommand(cmd);
			if (!cmd.hasSucceeded()) {
				po.addLogFailed("Failed to configure Hive server");
				return;
			}
			po.addLog("Server successfully installed");

			po.addLog("Installing clients...");
			s = Commands.make(CommandType.INSTALL, Product.HIVE);
			cmd = manager.getCommandRunner().createCommand(
					new RequestBuilder(s).withTimeout(120), config.getClients());
			manager.getCommandRunner().runCommand(cmd);

			if (cmd.hasCompleted()) {
				List<Agent> readyClients = new ArrayList<>();
				for (Agent a : config.getClients()) {
					res = cmd.getResults().get(a.getUuid());
					if (isZero(res.getExitCode())) {
						readyClients.add(a);
						po.addLog("Hive successfully installed on " + a.getHostname());
					} else
						po.addLog("Failed to install Hive on " + a.getHostname());
				}
				if (readyClients.size() > 0) {
					s = Commands.configureClient(config.getServer());
					cmd = manager.getCommandRunner().createCommand(
							new RequestBuilder(s),
							new HashSet<>(readyClients));
					manager.getCommandRunner().runCommand(cmd);
					for (Agent a : readyClients) {
						res = cmd.getResults().get(a.getUuid());
						if (isZero(res.getExitCode()))
							po.addLog(String.format("Client node '%s' successfully configured",
									a.getHostname()));
						else
							po.addLog(String.format("Failed to configure client node '%s': %s",
									a.getHostname(), res.getStdErr()));
					}
					po.addLogDone("Done");
				}
			} else
				po.addLogFailed("Failed to install client(s): "
						+ cmd.getAllErrors());
		} else
			po.addLogFailed("Failed to save cluster info.\nInstallation aborted");
	}

}
