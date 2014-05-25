package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.storm.*;
import org.safehaus.subutai.shared.protocol.Agent;

public class InstallHandler extends AbstractHandler {

    private final ProductOperation po;
    private final Config config;

    public InstallHandler(StormImpl manager, Config config) {
        super(manager, config.getClusterName());
        this.config = config;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_NAME,
                "Install cluster " + config.getClusterName());
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        if(manager.getCluster(config.getClusterName()) != null) {
            po.addLogFailed(String.format("Cluster '%s' already exists",
                    config.getClusterName()));
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

        po.addLog("Check installed packages");
        Set<Agent> allNodes = new HashSet<>(config.getSupervisors());
        allNodes.add(config.getNimbus());

        Set<Agent> skipped = new HashSet<>();
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.LIST)),
                allNodes);
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            Iterator<Agent> it = allNodes.iterator();
            while(it.hasNext()) {
                Agent a = it.next();
                AgentResult res = cmd.getResults().get(a.getUuid());
                if(isZero(res.getExitCode())) {
                    if(res.getStdOut().contains(Commands.PACKAGE_NAME)) {
                        po.addLog("Storm already installed on " + a.getHostname());
                        skipped.add(a);
                    }
                } else {
                    po.addLog(res.getStdOut());
                    po.addLog(res.getStdErr());
                }
            }
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        // install package
        po.addLog("Installing Storm on nodes");
        allNodes.removeAll(skipped);
        if(allNodes.size() > 0) {
            String s = Commands.make(CommandType.INSTALL);
            int t = (int)TimeUnit.MINUTES.toSeconds(15);
            cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s).withTimeout(t), allNodes);
            manager.getCommandRunner().runCommand(cmd);
            if(cmd.hasCompleted()) {
                boolean masterFailed = false;
                Iterator<Agent> it = allNodes.iterator();
                while(it.hasNext()) {
                    Agent a = it.next();
                    AgentResult res = cmd.getResults().get(a.getUuid());
                    if(isZero(res.getExitCode()))
                        po.addLog("Storm successfully installed on " + a.getHostname());
                    else if(isNimbusNode(config, a.getHostname())) {
                        po.addLog("Failed to install on master node");
                        masterFailed = true;
                    } else {
                        po.addLog("Failed to install on " + a.getHostname());
                        config.getSupervisors().remove(a);
                    }
                }
                if(masterFailed) {
                    po.addLogFailed(null);
                    return;
                }
            } else {
                po.addLog(cmd.getAllErrors());
                po.addLogFailed("Installation not completed");
                return;
            }
        }

        boolean saved = manager.getDbManager().saveInfo(Config.PRODUCT_NAME,
                clusterName, config);
        if(!saved) {
            po.addLogFailed("Failed to save cluster info");
            return;
        }
        po.addLog("Cluster info successfully saved");

        if(configure(config))
            po.addLogDone("Storm cluster successfully configured");
        else
            po.addLogFailed("Failed to configure Storm cluster");
    }

}
