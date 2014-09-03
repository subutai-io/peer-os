package org.safehaus.subutai.plugin.hive.impl.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.CommandType;
import org.safehaus.subutai.plugin.hive.impl.Commands;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
import org.safehaus.subutai.plugin.hive.impl.Product;

public class DestroyNodeHandler extends AbstractHandler {

    private final String hostname;

    public DestroyNodeHandler(HiveImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                HiveConfig.PRODUCT_KEY, "Remove node from cluster: " + hostname);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        HiveConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
        if(agent == null) {
            po.addLogFailed(String.format("Node '%s' is not connected", hostname));
            return;
        }
        if(!config.getClients().contains(agent)) {
            po.addLogFailed("Node does not belong to Sqoop installation group");
            return;
        }
        if(config.getClients().size() == 1) {
            po.addLog("This is the last node in cluster. Destroy cluster instead");
            return;
        }

        String s = Commands.make(CommandType.PURGE, Product.HIVE);
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s),
                new HashSet<>(Arrays.asList(agent)));
        manager.getCommandRunner().runCommand(cmd);

        AgentResult res = cmd.getResults().get(agent.getUuid());
        po.addLog(res.getStdOut());
        po.addLog(res.getStdErr());

        if(cmd.hasSucceeded()) {
            config.getClients().remove(agent);
            po.addLog("Done");

            po.addLog("Update cluster info...");
            if(manager.getDbManager().saveInfo(HiveConfig.PRODUCT_KEY, config.getClusterName(), config))
                po.addLogDone("Cluster info successfully updated");
            else
                po.addLogFailed("Failed to update cluster info");

        }
    }

}
