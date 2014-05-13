package org.safehaus.kiskis.mgmt.impl.flume.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.flume.CommandType;
import org.safehaus.kiskis.mgmt.impl.flume.Commands;
import org.safehaus.kiskis.mgmt.impl.flume.FlumeImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class StopHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;
    private final ProductOperation po;

    public StopHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Stop node " + hostname);
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

        po.addLog("Stopping node...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STOP)),
                new HashSet<Agent>(Arrays.asList(node)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded())
            po.addLogDone("Flume stopped on " + hostname);
        else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to stop node " + hostname);
        }

    }

}
