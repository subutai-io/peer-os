package org.safehaus.kiskis.mgmt.impl.solr.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public StartNodeOperationHandler(SolrImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostname, clusterName));
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

        po.addLog("Starting node...");

        Command startCommand = manager.getCommands().getStartCommand(node);
        manager.getCommandRunner().runCommand(startCommand);
        Command statusCommand = manager.getCommands().getStatusCommand(node);
        manager.getCommandRunner().runCommand(statusCommand);
        AgentResult result = statusCommand.getResults().get(node.getUuid());
        NodeState nodeState = NodeState.UNKNOWN;
        if (result != null) {
            if (result.getStdOut().contains("is running")) {
                nodeState = NodeState.RUNNING;
            } else if (result.getStdOut().contains("is not running")) {
                nodeState = NodeState.STOPPED;
            }
        }

        if (NodeState.RUNNING.equals(nodeState)) {
            po.addLogDone(String.format("Node on %s started", lxcHostname));
        } else {
            po.addLogFailed(String.format("Failed to start node %s. %s",
                    lxcHostname, startCommand.getAllErrors()
            ));
        }

    }
}
