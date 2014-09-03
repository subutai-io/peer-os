package org.safehaus.subutai.impl.zookeeper.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandCallback;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dilshat on 5/7/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
	private final ProductOperation po;
	private final String lxcHostname;


	public DestroyNodeOperationHandler(ZookeeperImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Destroying %s in %s", lxcHostname, clusterName));
	}


	@Override
	public UUID getTrackerId() {
		return po.getId();
	}


	@Override
	public void run() {
		final Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (agent == null) {
			po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
			return;
		}
		if (!config.getNodes().contains(agent)) {
			po.addLogFailed(
					String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
			return;
		}

		if (config.getNodes().size() == 1) {
			po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead");
			return;
		}

		if (config.isStandalone()) {

			//destroy lxc
			po.addLog("Destroying lxc container...");
			Agent physicalAgent = manager.getAgentManager().getAgentByHostname(agent.getParentHostName());
			if (physicalAgent == null) {
				po.addLog(String.format(
						"Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
						agent.getHostname()));
			} else {
				if (!manager.getLxcManager().destroyLxcOnHost(physicalAgent, agent.getHostname())) {
					po.addLog("Could not destroy lxc container. Use LXC module to cleanup, skipping...");
				} else {
					po.addLog("Lxc container destroyed successfully");
				}
			}
		} else {
			po.addLog("Uninstalling Zookeeper...");

			Command cmd = Commands.getUninstallCommand(Sets.newHashSet(agent));

			manager.getCommandRunner().runCommand(cmd);

			if (cmd.hasSucceeded()) {
				po.addLog("Zookeeper uninstalled");
			} else {
				po.addLog(String.format("Failed to uninstall Zookeeper, %s, skipping...", cmd.getAllErrors()));
			}
		}

		config.getNodes().remove(agent);

		//update settings
		po.addLog("Updating settings...");
		Command updateSettingsCommand = Commands.getUpdateSettingsCommand(config.getNodes());
		manager.getCommandRunner().runCommand(updateSettingsCommand);

		if (updateSettingsCommand.hasSucceeded()) {
			po.addLog("Settings updated\nRestarting cluster...");
			//restart all other nodes with new configuration
			Command restartCommand = Commands.getRestartCommand(config.getNodes());
			final AtomicInteger count = new AtomicInteger();
			manager.getCommandRunner().runCommand(restartCommand, new CommandCallback() {
				@Override
				public void onResponse(Response response, AgentResult agentResult, Command command) {
					if (agentResult.getStdOut().contains("STARTED")) {
						if (count.incrementAndGet() == config.getNodes().size()) {
							stop();
						}
					}
				}
			});

			if (count.get() == config.getNodes().size()) {
				po.addLog("Cluster successfully restarted");
			} else {
				po.addLog(
						String.format("Failed to restart cluster, %s, skipping...", restartCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed(String.format("Settings update failed, %s", updateSettingsCommand.getAllErrors()));
			return;
		}

		//update db
		po.addLog("Updating db...");

		try {
			manager.getDbManager().saveInfo2(Config.PRODUCT_KEY, config.getClusterName(), config);
			po.addLogDone("Information updated");
		} catch (DBException e) {
			po.addLogFailed(String.format("Failed to update information, %s", e.getMessage()));
		}
	}
}
