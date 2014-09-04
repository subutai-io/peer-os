package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;

public class DestroyNodeHandler extends AbstractHandler {

    private final String hostname;

    public DestroyNodeHandler(StormImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.productOperation = manager.getTracker().createProductOperation(
                StormConfig.PRODUCT_NAME,
                "Remove node from cluster: " + hostname);
        this.hostname = hostname;
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        StormConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("Node '%s' is not connected", hostname));
            return;
        }
        if(!config.getSupervisors().contains(agent)) {
            po.addLogFailed("Node is not a member of cluster");
            return;
        }
        if(config.getSupervisors().size() == 1) {
            po.addLogFailed("This is the last node in cluster. Destroy cluster instead");
            return;
        }

        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.PURGE)),
                new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Storm removed from " + hostname);

            try {
                po.addLog("Destroying container...");
                manager.getLxcManager().destroyLxcs(new HashSet<>(Arrays.asList(agent)));
                po.addLog("Container destroyed");

                config.getSupervisors().remove(agent);
                manager.getPluginDao().saveInfo(StormConfig.PRODUCT_NAME,
                        clusterName, config);
                po.addLogDone("Saved cluster info");
            } catch(LxcDestroyException ex) {
                po.addLogFailed("Failed to destroy node: " + ex.getMessage());
                manager.getLogger().error("Destroy failed", ex);
            } catch(DBException ex) {
                String m = "Failed to save cluster info";
                manager.getLogger().error(m, ex);
                po.addLogFailed(m);
            }
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove node from cluster");
        }
    }

}
