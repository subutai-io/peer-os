package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.*;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;

public class UninstallHandler extends AbstractHandler {

    public UninstallHandler(StormImpl manager, String clusterName) {
        super(manager, clusterName);
        this.productOperation = manager.getTracker().createProductOperation(
                StormConfig.PRODUCT_NAME,
                "Uninstall cluster " + clusterName);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        StormConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed("Cluster not found: " + clusterName);
            return;
        }
        if(!isNodeConnected(config.getNimbus().getHostname())) {
            po.addLogFailed(String.format("Master node %s is not connected",
                    config.getNimbus().getHostname()));
            return;
        }
        // check worker nodes
        if(checkSupervisorNodes(config, true) == 0) {
            po.addLogFailed("Worker nodes not connected");
            return;
        }

        Set<Agent> allNodes = new HashSet<>(config.getSupervisors());
        allNodes.add(config.getNimbus());
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.PURGE)),
                allNodes);
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            for(Agent a : allNodes) {
                AgentResult res = cmd.getResults().get(a.getUuid());
                if(isZero(res.getExitCode()))
                    po.addLog("Storm successfully removed from " + a.getHostname());
                else {
                    po.addLog(res.getStdOut());
                    po.addLog(res.getStdErr());
                }
            }
            try {
                po.addLog("Destroying container(s)...");
                manager.getLxcManager().destroyLxcs(allNodes);
                po.addLog("Container(s) destroyed");

                manager.getPluginDao().deleteInfo(StormConfig.PRODUCT_NAME, clusterName);
                po.addLogDone("Cluster info deleted");
            } catch(DBException ex) {
                manager.getLogger().error("Failed to delete from db", ex);
                po.addLogFailed("Failed to delete cluster info");
            } catch(LxcDestroyException ex) {
                po.addLog("Failed to destroy nodes: " + ex.getMessage());
                manager.getLogger().error("Destroying container(s) failed", ex);
            }
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove Storm on nodes");
        }

    }

}
