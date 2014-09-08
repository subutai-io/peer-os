package org.safehaus.subutai.impl.flume.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.flume.CommandType;
import org.safehaus.subutai.impl.flume.Commands;
import org.safehaus.subutai.impl.flume.FlumeImpl;
import org.safehaus.subutai.common.protocol.Agent;

public class UninstallHandler extends AbstractOperationHandler<FlumeImpl>
{

	public UninstallHandler(FlumeImpl manager, String clusterName) {
		super(manager, clusterName);
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY, "Destroy cluster " + clusterName);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed("Cluster does not exist: " + clusterName);
			return;
		}

		// check if nodes are connected
		for (Agent a : config.getNodes()) {
			Agent agent = manager.getAgentManager().getAgentByHostname(a.getHostname());
			if (agent == null) {
				po.addLogFailed(String.format(
						"Node %s is not connected. Operations aborted.",
						a.getHostname()));
				return;
			}
		}

		po.addLog("Uninstalling Flume...");

		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(Commands.make(CommandType.PURGE)),
				config.getNodes());
		manager.getCommandRunner().runCommand(cmd);

		if (cmd.hasCompleted()) {
			for (Agent agent : config.getNodes()) {
				AgentResult result = cmd.getResults().get(agent.getUuid());
				if (result.getExitCode() != null && result.getExitCode() == 0)
					if (result.getStdOut().contains("Flume is not installed"))
						po.addLog("Flume not installed on " + agent.getHostname());
					else
						po.addLog(String.format("Flume removed from node %s",
								agent.getHostname()));
				else
					po.addLog(String.format("Error on node %s: %s",
							agent.getHostname(), result.getStdErr()));
			}

			po.addLog("Updating db...");
			if (manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName()))
				po.addLogDone("Cluster info deleted from DB\nDone");
			else
				po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
		} else {
			po.addLog(cmd.getAllErrors());
			po.addLogFailed("Uninstallation failed");
		}
	}

}
