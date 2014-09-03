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

import java.util.Arrays;
import java.util.HashSet;

public class StartHandler extends AbstractOperationHandler<FlumeImpl>
{

	private final String hostname;

	public StartHandler(FlumeImpl manager, String clusterName, String hostname) {
		super(manager, clusterName);
		this.hostname = hostname;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY, "Start node " + hostname);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		if (manager.getCluster(clusterName) == null) {
			po.addLogFailed("Cluster does not exist: " + clusterName);
			return;
		}

		Agent node = manager.getAgentManager().getAgentByHostname(hostname);
		if (node == null) {
			po.addLogFailed("Node is not connected: " + hostname);
			return;
		}

		po.addLog("Starting node...");
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(Commands.make(CommandType.START)),
				new HashSet<>(Arrays.asList(node)));
		manager.getCommandRunner().runCommand(cmd);

		if (cmd.hasSucceeded())
			po.addLogDone("Flume started on " + hostname);
		else {
			AgentResult res = cmd.getResults().get(node.getUuid());
			if (res.getStdOut().contains("agent running"))
				po.addLogDone("Flume already started on " + hostname);
			else {
				po.addLog(res.getStdOut());
				po.addLog(res.getStdErr());
				po.addLogFailed("Failed to start node " + hostname);
			}
		}

	}

}
