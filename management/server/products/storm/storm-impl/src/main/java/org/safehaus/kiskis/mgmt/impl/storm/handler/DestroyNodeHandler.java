package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.storm.CommandType;
import org.safehaus.kiskis.mgmt.impl.storm.Commands;
import org.safehaus.kiskis.mgmt.impl.storm.StormImpl;
import org.safehaus.subutai.shared.protocol.Agent;

public class DestroyNodeHandler extends AbstractHandler {

    private final ProductOperation po;
    private final String hostname;

    public DestroyNodeHandler(StormImpl manager, String clusterName, String hostname) {
        super(manager, clusterName);
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_NAME,
                "Remove node from cluster: " + hostname);
        this.hostname = hostname;
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
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

            config.getSupervisors().remove(agent);
            boolean b = manager.getDbManager().saveInfo(Config.PRODUCT_NAME,
                    clusterName, config);
            if(b) po.addLogDone("Saved cluster info");
            else po.addLogFailed("Failed to save cluster info");
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove node from cluster");
        }
    }

}
