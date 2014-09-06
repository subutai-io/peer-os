package org.safehaus.subutai.plugin.spark.impl.handler;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
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
        if(config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        for(Agent node : config.getAllNodes()) {
            if(manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
                po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                return;
            }
        }

        po.addLog("Uninstalling Spark...");

        Command uninstallCommand = Commands.getUninstallCommand(config.getAllNodes());
        manager.getCommandRunner().runCommand(uninstallCommand);

        if(uninstallCommand.hasCompleted()) {
            for(AgentResult result : uninstallCommand.getResults().values()) {
                Agent agent = manager.getAgentManager().getAgentByUUID(result.getAgentUUID());
                if(result.getExitCode() != null && result.getExitCode() == 0)
                    if(result.getStdOut().contains("Spark is not installed, so not removed"))
                        po.addLog(String.format("Spark is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname()));
                    else
                        po.addLog(String.format("Spark is removed from node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname()));
                else
                    po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                            agent == null ? result.getAgentUUID() : agent.getHostname()));
            }
            po.addLog("Updating db...");
            try {
                manager.getPluginDAO().deleteInfo(SparkClusterConfig.PRODUCT_KEY, config.getClusterName());
                po.addLogDone("Cluster info deleted from DB\nDone");
            } catch(DBException ex) {
                po.addLogFailed("Error while deleting cluster info from DB: " + ex.getMessage());
            }
        } else
            po.addLogFailed(String.format("Uninstallation failed, %s", uninstallCommand.getAllErrors()));
    }
}
