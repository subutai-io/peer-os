package org.safehaus.kiskis.mgmt.impl.flume.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.flume.CommandType;
import org.safehaus.kiskis.mgmt.impl.flume.Commands;
import org.safehaus.kiskis.mgmt.impl.flume.FlumeImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StartHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;
    private final ProductOperation po;

    public StartHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Start node " + hostname);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

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

        po.addLog("Starting node...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.START)),
                new HashSet<Agent>(Arrays.asList(node)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded())
            po.addLogDone("Flume started on " + hostname);
        else {
            AgentResult res = cmd.getResults().get(node.getUuid());
            if(res.getStdOut().contains("agent running"))
                po.addLogDone("Flume already started on " + hostname);
            else {
                po.addLog(res.getStdOut());
                po.addLog(res.getStdErr());
                po.addLogFailed("Failed to start node " + hostname);
            }
        }

    }

}
