package org.safehaus.subutai.impl.sqoop.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.impl.sqoop.CommandFactory;
import org.safehaus.subutai.impl.sqoop.CommandType;
import org.safehaus.subutai.impl.sqoop.SqoopImpl;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class InstallHandler extends AbstractHandler {

	private Config config;

	public InstallHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
		super(manager, clusterName, po);
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public void run() {
		if (getClusterConfig() != null) {
			po.addLogFailed("Sqoop installation already exists: " + config.getClusterName());
			return;
		}
		if (checkNodes(config, true) == 0) {
			po.addLogFailed("No nodes are connected");
			return;
		}

		org.safehaus.subutai.api.hadoop.Config hc
				= manager.getHadoopManager().getCluster(clusterName);
		if (hc == null) {
			po.addLogFailed(String.format("Hadoop cluster %s does not exist", clusterName));
			return;
		}

		for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
			Agent a = it.next();
			if (!hc.getAllNodes().contains(a)) {
				po.addLog(String.format("Node %s is not in Hadoop cluster %s",
						a.getHostname(), clusterName));
				po.addLog("Removing from nodes list");
				it.remove();
			}
		}
		if (config.getNodes().isEmpty()) {
			po.addLogFailed("No nodes to install");
			return;
		}

		// check if already installed
		String s = CommandFactory.build(CommandType.LIST, null);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s), config.getNodes());
		manager.getCommandRunner().runCommand(cmd);

		Set<UUID> skipInstall = new HashSet<>();
		if (cmd.hasCompleted()) {
			Iterator<Agent> it = config.getNodes().iterator();
			while (it.hasNext()) {
				Agent a = it.next();
				AgentResult res = cmd.getResults().get(a.getUuid());
				if (isZero(res.getExitCode())) {
					if (res.getStdOut().contains(CommandFactory.PACKAGE_NAME)) {
						po.addLog(String.format("%s already installed on %s",
								CommandFactory.PACKAGE_NAME, a.getHostname()));
						skipInstall.add(a.getUuid());
					}
				} else {
					po.addLog(String.format("Failed to check installed packages on %s: %s",
							a.getHostname(), res.getStdErr()));
				}
			}
		} else {
			po.addLog(cmd.getAllErrors());
			po.addLogFailed("Failed to check installed packages");
			return;
		}

		if (config.getNodes().size() == skipInstall.size()) {
			saveClusterInfo(config);
			po.addLogDone("No nodes for installation");
			return;
		}

		// installation
		s = CommandFactory.build(CommandType.INSTALL, null);
		cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s).withTimeout(60), config.getNodes());
		manager.getCommandRunner().runCommand(cmd);

		if (cmd.hasCompleted()) {
			Iterator<Agent> it = config.getNodes().iterator();
			while (it.hasNext()) {
				Agent a = it.next();
				if (skipInstall.contains(a.getUuid())) continue;
				AgentResult res = cmd.getResults().get(a.getUuid());
				if (isZero(res.getExitCode())) {
					po.addLog("Successfully installed on " + a.getHostname());
				} else {
					it.remove();
					po.addLog("Failed to install on " + a.getHostname());
					po.addLog(res.getStdErr());
				}
			}
			if (config.getNodes().isEmpty()) {
				po.addLogFailed("Installation failed");
			} else {
				// save cluster info
				boolean b = saveClusterInfo(config);
				if (b) po.addLogDone("Installation completed");
				else po.addLogFailed(null);
			}
		} else {
			po.addLogFailed(cmd.getAllErrors());
		}
	}

	private boolean saveClusterInfo(Config config) {
		boolean saved = manager.getDbManager().saveInfo(Config.PRODUCT_KEY,
				config.getClusterName(), config);
		if (saved) po.addLog("Installation info successfully saved");
		else po.addLog("Failed to save installation info");
		return saved;
	}

}
