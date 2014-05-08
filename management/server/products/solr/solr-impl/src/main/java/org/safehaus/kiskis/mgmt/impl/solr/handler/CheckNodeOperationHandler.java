package org.safehaus.kiskis.mgmt.impl.solr.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public CheckNodeOperationHandler(SolrImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (node == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
            return;
        }
        if (!config.getNodes().contains(node)) {
            po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
            return;
        }
        po.addLog("Checking node...");

        Command checkNodeCommand = Commands.getStatusCommand(node);
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
            po.addLogFailed(String.format("Failed to check status of %s, %s",
                    lxcHostname, checkNodeCommand.getAllErrors()
            ));
        } else {
            po.addLogDone(String.format("Node %s is %s",
                    lxcHostname, nodeState
            ));
        }
    }
}
