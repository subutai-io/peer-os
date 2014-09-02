package org.safehaus.subutai.impl.storm.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.storm.CommandType;
import org.safehaus.subutai.impl.storm.Commands;
import org.safehaus.subutai.impl.storm.StormImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.HashSet;
import java.util.Set;

public class UninstallHandler extends AbstractHandler {

	public UninstallHandler(StormImpl manager, String clusterName) {
		super(manager, clusterName);
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_NAME,
				"Uninstall cluster " + clusterName);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed("Cluster not found: " + clusterName);
			return;
		}
		if (!isNodeConnected(config.getNimbus().getHostname())) {
			po.addLogFailed(String.format("Master node %s is not connected",
					config.getNimbus().getHostname()));
			return;
		}
		// check worker nodes
		if (checkSupervisorNodes(config, true) == 0) {
			po.addLogFailed("Worker nodes not connected");
			return;
		}

		Set<Agent> allNodes = new HashSet<>(config.getSupervisors());
		allNodes.add(config.getNimbus());
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(Commands.make(CommandType.PURGE)),
				allNodes);
		manager.getCommandRunner().runCommand(cmd);

		if (cmd.hasCompleted()) {
			for (Agent a : allNodes) {
				AgentResult res = cmd.getResults().get(a.getUuid());
				if (isZero(res.getExitCode()))
					po.addLog("Storm successfully removed from " + a.getHostname());
				else {
					po.addLog(res.getStdOut());
					po.addLog(res.getStdErr());
				}
			}
			boolean b = manager.getDbManager().deleteInfo(Config.PRODUCT_NAME, clusterName);
			if (b) {
				try {
					manager.getLxcManager().destroyLxcs(allNodes);
				} catch (LxcDestroyException ex) {
					po.addLog("Failed to destroy nodes: " + ex.getMessage());
				}
				po.addLogDone("Cluster info deleted");
			} else po.addLogFailed("Failed to delete cluster info");
		} else {
			po.addLog(cmd.getAllErrors());
			po.addLogFailed("Failed to remove Storm on nodes");
		}

	}

}
