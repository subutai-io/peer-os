package org.safehaus.subutai.impl.solr.handler;


import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.common.protocol.Agent;


public class CheckNodeOperationHandler extends AbstractOperationHandler<SolrImpl>
{
	private final String lxcHostname;


	public CheckNodeOperationHandler(SolrImpl manager, String clusterName, String lxcHostname) {
		super(manager, clusterName);
		this.lxcHostname = lxcHostname;
		productOperation = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
				String.format("Checking node %s in %s", lxcHostname, clusterName));
	}


	@Override
	public void run() {
		Config config = manager.getCluster(clusterName);

		if (config == null) {
			productOperation.addLogFailed(
					String.format("Installation with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);

		if (node == null) {
			productOperation.addLogFailed(
					String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}

		if (!config.getNodes().contains(node)) {
			productOperation.addLogFailed(
					String.format("Agent with hostname %s does not belong to installation %s", lxcHostname, clusterName));
			return;
		}

		productOperation.addLog("Checking node...");

		Command checkNodeCommand = manager.getCommands().getStatusCommand(node);
		manager.getCommandRunner().runCommand(checkNodeCommand);

		NodeState nodeState = NodeState.UNKNOWN;

		if (checkNodeCommand.hasCompleted()) {
			AgentResult result = checkNodeCommand.getResults().get(node.getUuid());

			if (result.getStdOut().contains("is running")) {
				nodeState = NodeState.RUNNING;
			} else if (result.getStdOut().contains("is not running")) {
				nodeState = NodeState.STOPPED;
			}
		}

		if (NodeState.UNKNOWN.equals(nodeState)) {
			productOperation.addLogFailed(
					String.format("Failed to check status of %s, %s", lxcHostname, checkNodeCommand.getAllErrors()));
		} else {
			productOperation.addLogDone(String.format("Node %s is %s", lxcHostname, nodeState));
		}
	}
}
