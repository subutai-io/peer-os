package org.safehaus.subutai.plugin.flume.impl.handler;

import java.util.*;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;

public class DestroyNodeHandler extends AbstractOperationHandler<FlumeImpl> {

    private final String hostname;

    public DestroyNodeHandler(FlumeImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Remove node from cluster: " + hostname);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        FlumeConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed("Node is not connected: " + hostname);
            return;
        }
        if(!config.getNodes().contains(agent)) {
            po.addLogFailed("Node does not belong to Flume installation group");
            return;
        }
        if(config.getNodes().size() == 1) {
            po.addLogFailed("This is the last node in the cluster. Destroy cluster instead");
            return;
        }

        boolean ok = uninstallFlume(agent);

        if(ok) {
            po.addLog("Updating db...");
            config.getNodes().remove(agent);
            try {
                manager.getPluginDao().saveInfo(FlumeConfig.PRODUCT_KEY, config.getClusterName(), config);
                po.addLogDone("Cluster info updated");
            } catch(DBException ex) {
                po.addLogFailed("Failed to save cluster info");
                manager.getLogger().error("Failed to save cluster info", ex);
            }
        } else po.addLogFailed(null);
    }

    private boolean uninstallFlume(Agent agent) {
        ProductOperation po = productOperation;
        po.addLog("Uninstalling Flume...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.PURGE)),
                new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded()) {
            po.addLog("Flume removed from " + agent.getHostname());
            return true;
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLog("Uninstallation failed");
            return false;
        }
    }

}
