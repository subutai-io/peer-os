package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.UUID;


/**
 * Created by dilshat on 5/6/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
	private final ProductOperation po;
	private final String lxcHostname;
	private final NodeType nodeType;


	public DestroyNodeOperationHandler(AccumuloImpl manager, String clusterName, String lxcHostname,
	                                   NodeType nodeType) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		this.nodeType = nodeType;
		po = manager.getTracker().createProductOperation(AccumuloClusterConfig.PRODUCT_KEY,
				String.format("Destroying %s on %s", nodeType, lxcHostname));
	}


	@Override
	public UUID getTrackerId() {
		return po.getId();
	}


	@Override
	public void run() {
		if (!(nodeType == NodeType.TRACER || nodeType.isSlave())) {
			po.addLogFailed("Only tracer or slave node can be destroyed");
			return;
		}

		final AccumuloClusterConfig accumuloClusterConfig = manager.getCluster(clusterName);
		if (accumuloClusterConfig == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (agent == null) {
			po.addLogFailed(
					String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}
		if (!accumuloClusterConfig.getAllNodes().contains(agent)) {
			po.addLogFailed(
					String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
			return;
		}
		if (manager.getAgentManager().getAgentByHostname(accumuloClusterConfig.getMasterNode().getHostname())
				== null) {
			po.addLogFailed(String.format("Master node %s is not connected\nOperation aborted",
					accumuloClusterConfig.getMasterNode().getHostname()));
			return;
		}

		if (nodeType == NodeType.TRACER) {
			if (accumuloClusterConfig.getTracers().size() == 1) {
				po.addLogFailed("This is the last tracer in the cluster, destroy cluster instead\nOperation aborted");
				return;
			}
			accumuloClusterConfig.getTracers().remove(agent);
		} else {
			if (accumuloClusterConfig.getSlaves().size() == 1) {
				po.addLogFailed("This is the last slave in the cluster, destroy cluster instead\nOperation aborted");
				return;
			}
			accumuloClusterConfig.getSlaves().remove(agent);
		}

		boolean uninstall = !accumuloClusterConfig.getAllNodes().contains(agent);

		if (uninstall) {
			po.addLog("Uninstalling Accumulo...");

			Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
			manager.getCommandRunner().runCommand(uninstallCommand);

			if (uninstallCommand.hasSucceeded()) {
				po.addLog("Accumulo uninstallation succeeded");
			} else {
				po.addLog(String.format("Accumulo uninstallation failed, %s, skipping...",
						uninstallCommand.getAllErrors()));
			}
		}

		Command unregisterNodeCommand;
		if (nodeType == NodeType.TRACER) {
			unregisterNodeCommand = Commands.getClearTracerCommand(accumuloClusterConfig.getAllNodes(), agent);
		} else {
			unregisterNodeCommand = Commands.getClearSlaveCommand(accumuloClusterConfig.getAllNodes(), agent);
		}

		po.addLog("Unregistering node from cluster...");
		manager.getCommandRunner().runCommand(unregisterNodeCommand);

		if (unregisterNodeCommand.hasSucceeded()) {
			po.addLog("Node unregistered successfully\nRestarting cluster...");

			Command restartClusterCommand = Commands.getRestartCommand(accumuloClusterConfig.getMasterNode());
			manager.getCommandRunner().runCommand(restartClusterCommand);
			if (restartClusterCommand.hasSucceeded()) {
				po.addLog("Cluster restarted successfully");
			} else {
				po.addLog(String.format("Cluster restart failed, %s, skipping...",
						restartClusterCommand.getAllErrors()));
			}

			po.addLog("Updating db...");
			if (manager.getDbManager()
					.saveInfo(AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName(),
							accumuloClusterConfig)) {
				po.addLogDone("Cluster info updated\nDone");
			} else {
				po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
						accumuloClusterConfig.getClusterName()));
			}
		} else {
			po.addLogFailed(String.format("Unregistering node failed, %s\nOperation aborted",
					unregisterNodeCommand.getAllErrors()));
		}
	}
}
