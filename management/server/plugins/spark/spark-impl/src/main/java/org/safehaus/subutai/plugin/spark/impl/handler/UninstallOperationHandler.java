package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

public class UninstallOperationHandler extends AbstractOperationHandler<SparkImpl> {

    public UninstallOperationHandler(SparkImpl manager, String clusterName) {
        super(manager, clusterName);
        productOperation = manager.getTracker().createProductOperation(SparkClusterConfig.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        SparkClusterConfig config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        for (Agent node : config.getAllNodes()) {
            if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
                po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                return;
            }
        }

        boolean ok = false;
        if (config.getSetupType() == SetupType.OVER_HADOOP)
            ok = uninstall(config);
        else if (config.getSetupType() == SetupType.WITH_HADOOP)
            ok = destroyNodes(config);
        else
            po.addLog("Undefined setup type");

        if (ok) {
            po.addLog("Updating db...");
            try {
                manager.getPluginDAO().deleteInfo(SparkClusterConfig.PRODUCT_KEY,
                        config.getClusterName());
                po.addLogDone("Cluster info deleted from DB\nDone");
            } catch (DBException e) {
                po.addLogFailed("Failed to delete cluster info from DB");
            }
        } else
            po.addLogFailed("Failed to destroy cluster");
    }

    private boolean uninstall(SparkClusterConfig config) {
        ProductOperation po = productOperation;
        po.addLog("Uninstalling Spark...");

        Command cmd = Commands.getUninstallCommand(config.getAllNodes());
        manager.getCommandRunner().runCommand(cmd);

        if (cmd.hasSucceeded()) return true;

        po.addLog(cmd.getAllErrors());
        po.addLogFailed("Uninstallation failed");
        return false;
    }

    private boolean destroyNodes(SparkClusterConfig config) {

        productOperation.addLog("Destroying node(s)...");
        try {
            manager.getContainerManager().clonesDestroy(config.getAllNodes());
            productOperation.addLog("Destroying node(s) completed");
            return true;
        } catch (LxcDestroyException ex) {
            productOperation.addLog("Failed to destroy node(s): " + ex.getMessage());
            return false;
        }
    }
}
