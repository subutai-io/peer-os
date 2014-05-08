package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.storm.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class UninstallHandler extends AbstractHandler {

    private final ProductOperation po;

    public UninstallHandler(StormImpl manager, String clusterName) {
        super(manager, clusterName);
        po = manager.getTracker().createProductOperation(Config.PRODUCT_NAME,
                "Uninstall cluster " + clusterName);
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    public void run() {
        Config config = manager.getCluster(clusterName);
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

        Set<Agent> allNodes = new HashSet<Agent>(config.getSupervisors());
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
            boolean b = manager.getDbManager().deleteInfo(Config.PRODUCT_NAME, clusterName);
            if(b) po.addLogDone("Cluster info deleted");
            else po.addLogFailed("Failed to delete cluster info");
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to remove Storm on nodes");
        }

    }

}
