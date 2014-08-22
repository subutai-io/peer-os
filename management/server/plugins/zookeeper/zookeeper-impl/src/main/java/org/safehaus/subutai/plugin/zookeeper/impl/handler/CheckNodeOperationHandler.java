package org.safehaus.subutai.plugin.zookeeper.impl.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Handles check node status operation
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public CheckNodeOperationHandler(ZookeeperImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format("Checking node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        ZookeeperClusterConfig config = manager.getCluster(clusterName);
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
        Command checkCommand = Commands.getStatusCommand(node);
        manager.getCommandRunner().runCommand(checkCommand);
        NodeState state = NodeState.UNKNOWN;
        if (checkCommand.hasCompleted()) {
            AgentResult result = checkCommand.getResults().get(node.getUuid());
            if (result.getStdOut().contains("is Running")) {
                state = NodeState.RUNNING;
            } else if (result.getStdOut().contains("is NOT Running")) {
                state = NodeState.STOPPED;
            }
        }

        if (NodeState.UNKNOWN.equals(state)) {
            po.addLogFailed(String.format("Failed to check status of %s, %s",
                    lxcHostname,
                    checkCommand.getAllErrors()
            ));
        } else {
            po.addLogDone(String.format("Node %s is %s",
                    lxcHostname,
                    state
            ));
        }
    }
}
