package org.safehaus.subutai.impl.flume.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.impl.flume.CommandType;
import org.safehaus.subutai.impl.flume.Commands;
import org.safehaus.subutai.impl.flume.FlumeImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddNodeHandler extends AbstractOperationHandler<FlumeImpl> {

	private final String hostname;

	public AddNodeHandler(FlumeImpl manager, String clusterName, String hostname) {
		super(manager, clusterName);
		this.hostname = hostname;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY, "Add node to cluster: " + clusterName);
	}

	@Override
	public void run() {
		ProductOperation po = this.productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed("Cluster does not exist: " + clusterName);
			return;
		}
		//check if node agent is connected
		Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
		if (agent == null) {
			po.addLogFailed("Node is not connected: " + hostname);
			return;
		}

		Set<Agent> set = new HashSet<>(Arrays.asList(agent));

		po.addLog("Checking prerequisites...");
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(Commands.make(CommandType.STATUS)), set);
		manager.getCommandRunner().runCommand(cmd);
		if (!cmd.hasSucceeded()) {
			po.addLogFailed("Failed to check installed packages");
			return;
		}

        AgentResult res = cmd.getResults().get(agent.getUuid());
        if(res.getStdOut().contains(Commands.PACKAGE_NAME)) {
            po.addLogFailed("Flume already installed on " + hostname);
            return;
        } else if(!res.getStdOut().contains("ksks-hadoop")) {
            po.addLogFailed("Hadoop not installed on " + hostname);
            return;
        }

		config.getNodes().add(agent);

		po.addLog("Updating db...");
		if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLog("Cluster info updated in DB\nInstalling Flume...");

			cmd = manager.getCommandRunner().createCommand(
					new RequestBuilder(Commands.make(CommandType.INSTALL)),
					set);
			manager.getCommandRunner().runCommand(cmd);

			if (cmd.hasSucceeded())
				po.addLogDone("Installation succeeded");
			else {
				po.addLog(cmd.getAllErrors());
				po.addLogFailed("Installation failed");
			}
		} else
			po.addLogFailed("Failed to update cluster info");

	}

}
