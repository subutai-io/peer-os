package org.safehaus.subutai.plugin.flume.impl.handler;

import java.util.*;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

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

        boolean ok = false;
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            ok = uninstallFlume(agent);
        else if(config.getSetupType() == SetupType.WITH_HADOOP)
            try {
                manager.getContainerManager().cloneDestroy(
                        agent.getParentHostName(), agent.getHostname());
                ok = true;
            } catch(LxcDestroyException ex) {
                String m = "Failed to destroy " + agent.getHostname();
                po.addLog(m);
                manager.getLogger().error(m, ex);
            }
        else po.addLog("Unsupported setup type: " + config.getSetupType());

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
