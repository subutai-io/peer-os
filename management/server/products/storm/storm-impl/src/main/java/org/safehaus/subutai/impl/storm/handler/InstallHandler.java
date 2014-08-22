package org.safehaus.subutai.impl.storm.handler;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.impl.storm.*;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.protocol.Agent;

public class InstallHandler extends AbstractHandler {

    private final Config config;

    public InstallHandler(StormImpl manager, Config config) {
        super(manager, config.getClusterName());
        this.config = config;
        this.productOperation = manager.getTracker().createProductOperation(
                Config.PRODUCT_NAME,
                "Install cluster " + config.getClusterName());
    }

    @Override
    public void run() {
        doInstallation();
        if(productOperation.getState() != ProductOperationState.SUCCEEDED)
            destroyNodes();
    }

    void doInstallation() {
        ProductOperation po = productOperation;
        if(manager.getCluster(config.getClusterName()) != null) {
            po.addLogFailed(String.format("Cluster '%s' already exists",
                    config.getClusterName()));
            return;
        }

        try {
            if(!prepareNodes(config)) {
                po.addLogFailed("Failed to prepare nodes");
                return;
            }
        } catch(LxcCreateException ex) {
            po.addLogFailed("Failed to create nodes: " + ex.getMessage());
            return;
        } catch(Exception ex) {
            po.addLogFailed("Failed to prepare nodes: " + ex.getMessage());
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

        po.addLog("Checking installed packages...");
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
        po.addLog("Installing Storm on nodes...");
        allNodes.removeAll(skipped);
        if(allNodes.size() > 0) {
            String s = Commands.make(CommandType.INSTALL);
            int t = (int)TimeUnit.MINUTES.toSeconds(25);
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
                        po.addLog("Failed to install on Nimbus node");
                        masterFailed = true;
                    } else {
                        po.addLog("Failed to install on " + a.getHostname());
                        po.addLog(String.format("Destroying container for %s...", a.getHostname()));
                        try {
                            manager.getLxcManager().destroyLxcs(new HashSet<>(Arrays.asList(a)));
                        } catch(LxcDestroyException ex) {
                            po.addLog("Failed to detroy container. Use LXC manager to clean up.");
                        }
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

    void destroyNodes() {
        ProductOperation po = productOperation;
        try {
            if(config.getSupervisors() != null && config.getSupervisors().size() > 0) {
                po.addLog("Destroying supervisor nodes...");
                manager.getLxcManager().destroyLxcs(config.getSupervisors());
            }
            if(config.getNimbus() != null && !config.isExternalZookeeper()) {
                po.addLog("Destroying Nimbus node...");
                HashSet<Agent> set = new HashSet<>(Arrays.asList(config.getNimbus()));
                manager.getLxcManager().destroyLxcs(set);
            }
        } catch(LxcDestroyException ex) {
            po.addLog("Failed to destroy nodes: " + ex.getMessage());
        }
    }

    private boolean prepareNodes(Config config) throws LxcCreateException {
        ProductOperation po = productOperation;
        InstallHelper helper = new InstallHelper(manager);
        // if no external Zookeeper instance specified, create new nimbus node
        if(!config.isExternalZookeeper()) {
            po.addLog("Creating container for Nimbus node...");
            Agent nimbus = helper.createContainer();
            if(nimbus == null) {
                po.addLogFailed("Failed to create nimbus node");
                return false;
            }
            config.setNimbus(nimbus);
            // install Zookeeper on nimbus
            po.addLog("Installing Zookeeper on Nimbus node...");
            boolean b = helper.installZookeeper(nimbus);
            if(!b) {
                po.addLogFailed("Failed to install Zookeeper on nimbus");
                return false;
            }
        }
        // create supervisor nodes
        po.addLog(String.format("Creating %s container(s) for supervisor nodes...",
                config.getSupervisorsCount()));
        Set<Agent> set = helper.createContainers(config.getSupervisorsCount());
        if(set.size() != config.getSupervisorsCount())
            po.addLog("Not all nodes created. Created nodes count: " + set.size());

        config.setSupervisors(set);
        config.setSupervisorsCount(set.size());
        return true;
    }

}
