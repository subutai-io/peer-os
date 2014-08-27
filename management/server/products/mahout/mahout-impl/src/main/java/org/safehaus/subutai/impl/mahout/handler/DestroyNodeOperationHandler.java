package org.safehaus.subutai.impl.mahout.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.mahout.Config;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.impl.mahout.Commands;
import org.safehaus.subutai.impl.mahout.MahoutImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<MahoutImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public DestroyNodeOperationHandler(MahoutImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        final Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (agent == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
            return;
        }

        if (!config.getNodes().contains(agent)) {
            po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
            return;
        }

        if (config.getNodes().size() == 1) {
            po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
            return;
        }
        po.addLog("Uninstalling Mahout...");
        Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
        manager.getCommandRunner().runCommand(uninstallCommand);

        if (uninstallCommand.hasCompleted()) {
            AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
            if (result.getExitCode() != null && result.getExitCode() == 0) {
                if (result.getStdOut().contains("Package ksks-mahout is not installed, so not removed")) {
                    po.addLog(String.format("Mahout is not installed, so not removed on node %s",
                            agent.getHostname()));
                } else {
                    po.addLog(String.format("Mahout is removed from node %s",
                            agent.getHostname()));
                }
            } else {
                po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                        agent.getHostname()));
            }

            config.getNodes().remove(agent);
            po.addLog("Updating db...");

            if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                po.addLogDone("Cluster info update in DB\nDone");
            } else {
                po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
            }
        } else {
            po.addLogFailed("Uninstallation failed, command timed out");
        }
    }
}
