package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

public class StopNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {

    private final String lxcHostname;
    private final boolean master;

    public StopNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname, boolean master) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        this.master = master;
        productOperation = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        SparkClusterConfig config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (node == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
            return;
        }

        if (!config.getAllNodes().contains(node)) {
            po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
            return;
        }

        if (master && !config.getMasterNode().equals(node)) {
            po.addLogFailed(String.format("Node %s is not a master node\nOperation aborted", node.getHostname()));
            return;
        } else if (!master && !config.getSlaveNodes().contains(node)) {
            po.addLogFailed(String.format("Node %s is not a slave node\nOperation aborted", node.getHostname()));
            return;
        }

        po.addLog(String.format("Stopping %s on %s...", master ? "master" : "slave", node.getHostname()));

        Command stopCommand;
        if (master)
            stopCommand = Commands.getStopMasterCommand(node);
        else
            stopCommand = Commands.getStopSlaveCommand(node);
        manager.getCommandRunner().runCommand(stopCommand);

        if (stopCommand.hasSucceeded())
            po.addLogDone(String.format("Node %s stopped", node.getHostname()));
        else
            po.addLogFailed(
                    String.format("Stopping %s failed, %s", node.getHostname(), stopCommand.getAllErrors()));
    }
}
