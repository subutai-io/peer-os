package org.safehaus.subutai.plugin.flume.impl.handler;

import java.util.*;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
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

        po.addLog("Uninstalling Flume...");
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.PURGE)),
                new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            AgentResult res = cmd.getResults().get(agent.getUuid());
            if(res.getExitCode() != null && res.getExitCode() == 0)
                po.addLog("Flume removed from " + agent.getHostname());
            else {
                po.addLog(res.getStdOut());
                po.addLog(res.getStdErr());
                po.addLogFailed("Failed to remove Flume on " + agent.getHostname());
                return;
            }

            config.getNodes().remove(agent);

            po.addLog("Updating db...");
            try {
                manager.getPluginDao().saveInfo(FlumeConfig.PRODUCT_KEY, config.getClusterName(), config);
                po.addLogDone("Cluster info updated");
            } catch(DBException ex) {
                po.addLogFailed("Failed to save cluster info");
                manager.getLogger().error("Failed to save cluster info", ex);
            }
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Uninstallation failed");
        }
    }

}
