package org.safehaus.subutai.plugin.presto.impl.handler;

import com.google.common.collect.Sets;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

public class StopNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {

    private final String lxcHostname;

    public StopNodeOperationHandler(PrestoImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation(PrestoClusterConfig.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        PrestoClusterConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if(node == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
            return;
        }

        if(!config.getAllNodes().contains(node)) {
            po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
            return;
        }

        po.addLog(String.format("Stopping node %s...", node.getHostname()));

        Command stopNodeCommand = Commands.getStopCommand(Sets.newHashSet(node));
        manager.getCommandRunner().runCommand(stopNodeCommand);

        if(stopNodeCommand.hasSucceeded())
            po.addLogDone(String.format("Node %s stopped", node.getHostname()));
        else
            po.addLogFailed(
                    String.format("Stopping %s failed, %s", node.getHostname(), stopNodeCommand.getAllErrors()));
    }
}
