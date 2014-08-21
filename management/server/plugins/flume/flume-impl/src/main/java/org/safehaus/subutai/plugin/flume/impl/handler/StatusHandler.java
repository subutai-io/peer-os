package org.safehaus.subutai.plugin.flume.impl.handler;

import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

public class StatusHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;

    public StatusHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Check status of " + hostname);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
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
