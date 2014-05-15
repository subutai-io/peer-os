package org.safehaus.kiskis.mgmt.impl.pig.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.pig.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.pig.Commands;
import org.safehaus.kiskis.mgmt.impl.pig.PigImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<PigImpl> {
    private final ProductOperation po;

    public UninstallOperationHandler(PigImpl manager, String clusterName) {
        super(manager, clusterName);
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));
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

        for (Agent node : config.getNodes()) {
            if (manager.getAgentManager().getAgentByHostname(node.getHostname()) == null) {
                po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                return;
            }
        }

        po.addLog("Uninstalling Pig...");

        Command uninstallCommand = Commands.getUninstallCommand(config.getNodes());
        manager.getCommandRunner().runCommand(uninstallCommand);

        if (uninstallCommand.hasCompleted()) {
            for (AgentResult result : uninstallCommand.getResults().values()) {
                Agent agent = manager.getAgentManager().getAgentByUUID(result.getAgentUUID());
                if (result.getExitCode() != null && result.getExitCode() == 0) {
                    if (result.getStdOut().contains("Package ksks-pig is not installed, so not removed")) {
                        po.addLog(String.format("Pig is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname()));
                    } else {
                        po.addLog(String.format("Pig is removed from node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname()));
                    }
                } else {
                    po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                            agent == null ? result.getAgentUUID() : agent.getHostname()));
                }
            }
            po.addLog("Updating db...");
            if (manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                po.addLogDone("Cluster info deleted from DB\nDone");
            } else {
                po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
            }
        } else {
            po.addLogFailed("Uninstallation failed, command timed out");
        }
    }
}
