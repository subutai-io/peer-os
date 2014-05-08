package org.safehaus.kiskis.mgmt.impl.storm.handler;

import java.util.*;
import org.safehaus.kiskis.mgmt.api.commandrunner.*;
import org.safehaus.kiskis.mgmt.api.storm.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.storm.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

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
        Set<Agent> allNodes = new HashSet<Agent>(config.getSupervisors());
        allNodes.add(config.getNimbus());

        Set<Agent> skipped = new HashSet<Agent>();
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.LIST)),
                allNodes);
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            Iterator<Agent> it = allNodes.iterator();
            while(it.hasNext()) {
                Agent a = it.next();
                AgentResult res = cmd.getResults().get(it.next().getUuid());
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

        boolean saved = manager.getDbManager().saveInfo(Config.PRODUCT_NAME,
                clusterName, config);
        if(!saved) {
            po.addLogFailed("Failed to save cluster info");
            return;
        }
        po.addLog("Cluster info successfully saved");

        // install package
        po.addLog("Installing Storm on nodes");
        allNodes.removeAll(skipped);
        if(allNodes.size() > 0) {
            String s = Commands.make(CommandType.INSTALL);
            cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s).withTimeout(60), allNodes);
            manager.getCommandRunner().runCommand(cmd);
            if(cmd.hasCompleted()) {
                Iterator<Agent> it = allNodes.iterator();
                while(it.hasNext()) {
                    Agent a = it.next();
                    AgentResult res = cmd.getResults().get(a.getUuid());
                    if(isZero(res.getExitCode()))
                        po.addLog("Storm successfully installed on " + a.getHostname());
                    else if(isNimbusNode(config, a.getHostname())) {
                        po.addLogFailed("Failed to install on master node");
                        return;
                    } else {
                        po.addLog("Failed to install on " + a.getHostname());
                        config.getSupervisors().remove(a);
                    }
                }
            }
        }

        if(configure())
            po.addLogDone("Storm cluster successfully configures");
        else
            po.addLogFailed("Failed to configure Storm cluster");
    }

    private boolean configure() {
        String zk_servers = makeZookeeperServersList();
        if(zk_servers == null) return false;

        Map<String, String> paramValues = new HashMap<String, String>();
        paramValues.put("storm.zookeeper.servers", zk_servers);
        paramValues.put("nimbus.host", config.getNimbus().getListIP().get(0));
        paramValues.put("storm.local.dir", "/var/lib/storm");

        Set<Agent> allNodes = new HashSet<Agent>(config.getSupervisors());
        allNodes.add(config.getNimbus());

        for(Map.Entry<String, String> e : paramValues.entrySet()) {
            String s = Commands.configure("add", "storm.xml", e.getKey(), e.getValue());
            Command cmd = manager.getCommandRunner().createCommand(
                    new RequestBuilder(s), allNodes);
            manager.getCommandRunner().runCommand(cmd);
            if(!cmd.hasSucceeded()) return false;
        }
        return true;
    }

    private String makeZookeeperServersList() {
        org.safehaus.kiskis.mgmt.api.zookeeper.Config zkConfig
                = manager.getZookeeperManager().getCluster(clusterName);
        if(zkConfig == null) return null;

        StringBuilder sb = new StringBuilder();
        for(Agent a : zkConfig.getNodes()) {
            if(sb.length() > 0) sb.append(",");
            sb.append(a.getListIP().get(0));
        }
        return sb.toString();
    }
}
