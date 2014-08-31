package org.safehaus.subutai.plugin.flume.impl.handler;

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

public class UninstallHandler extends AbstractOperationHandler<FlumeImpl> {

    public UninstallHandler(FlumeImpl manager, String clusterName) {
        super(manager, clusterName);
        this.productOperation = manager.getTracker().createProductOperation(
                FlumeConfig.PRODUCT_KEY, "Destroy cluster " + clusterName);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        FlumeConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }

        boolean ok = false;
        if(config.getSetupType() == SetupType.OVER_HADOOP)
            ok = uninstallFlume(config);
        else if(config.getSetupType() == SetupType.WITH_HADOOP)
            ok = destroyNodes(config);
        else
            po.addLog("Unsupported setup type: " + config.getSetupType());

        if(ok) {
            po.addLog("Updating db...");
            try {
                manager.getPluginDao().deleteInfo(FlumeConfig.PRODUCT_KEY, clusterName);
                po.addLogDone("Cluster info deleted from DB\nDone");
            } catch(DBException ex) {
                po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                manager.getLogger().error("Failed to delete cluster info", ex);
            }
        } else po.addLogFailed(null);
    }

    private boolean uninstallFlume(FlumeConfig config) {

        ProductOperation po = productOperation;
        // check if nodes are connected
        for(Agent a : config.getNodes()) {
            Agent agent = manager.getAgentManager().getAgentByHostname(a.getHostname());
            if(agent == null) {
                po.addLog(String.format(
                        "Node %s is not connected. Operations aborted.",
                        a.getHostname()));
                return false;
            }
        }

        po.addLog("Uninstalling Flume...");

        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.PURGE)),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            for(Agent agent : config.getNodes()) {
                AgentResult result = cmd.getResults().get(agent.getUuid());
                if(result.getExitCode() != null && result.getExitCode() == 0)
                    po.addLog(String.format("Flume removed from node %s",
                            agent.getHostname()));
                else
                    po.addLog(String.format("Failed to remove Flume on %s: %s",
                            agent.getHostname(), result.getStdErr()));
            }
            return true;
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLog("Uninstallation failed");
            return false;
        }
    }

    private boolean destroyNodes(FlumeConfig config) {
        ProductOperation po = productOperation;
        po.addLog("Destroying container(s)...");
        try {
            manager.getContainerManager().clonesDestroy(config.getHadoopNodes());
            po.addLog("Container(s) successfully destroyed");
        } catch(LxcDestroyException ex) {
            String m = "Failed to destroy container(s)";
            po.addLog(m);
            manager.getLogger().error(m, ex);
        }
        return true;
    }

}
