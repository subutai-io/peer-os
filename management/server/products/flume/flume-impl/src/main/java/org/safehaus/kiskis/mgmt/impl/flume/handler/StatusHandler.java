package org.safehaus.kiskis.mgmt.impl.flume.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.impl.flume.CommandType;
import org.safehaus.kiskis.mgmt.impl.flume.Commands;
import org.safehaus.kiskis.mgmt.impl.flume.FlumeImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

public class StatusHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;
    private final ProductOperation po;

    public StatusHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Check status of " + hostname);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        if(manager.getCluster(clusterName) == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname(hostname);
        if(node == null) {
            po.addLogFailed("Node is not connected: " + hostname);
            return;
        }

        po.addLog("Checking node...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)),
                new HashSet<>(Arrays.asList(node)));
        manager.getCommandRunner().runCommand(cmd);

        NodeState nodeState = NodeState.UNKNOWN;
        if(cmd.hasSucceeded()) {
            AgentResult result = cmd.getResults().get(node.getUuid());
            if(result.getStdOut().contains("is running"))
                nodeState = NodeState.RUNNING;
            else if(result.getStdOut().contains("is not running"))
                nodeState = NodeState.STOPPED;
        }

        if(NodeState.UNKNOWN.equals(nodeState))
            po.addLogFailed(String.format("Failed to check status of %s, %s",
                    hostname, cmd.getAllErrors()));
        else
            po.addLogDone(String.format("Node %s is %s", hostname,
                    nodeState));
    }

}
