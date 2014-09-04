package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.core.command.api.*;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.*;

public class InstallHandler extends AbstractHandler {

    private final StormConfig config;

    public InstallHandler(StormImpl manager, StormConfig config) {
        super(manager, config.getClusterName());
        this.config = config;
        this.productOperation = manager.getTracker().createProductOperation(
                StormConfig.PRODUCT_NAME,
                "Install cluster " + config.getClusterName());
    }

    @Override
    public void run() {
        Environment env = null;
        EnvironmentBlueprint eb = manager.getDefaultEnvironmentBlueprint(config);
        try {
            productOperation.addLog("Building environment...");
            env = manager.getEnvironmentManager().buildEnvironmentAndReturn(eb);
            productOperation.addLog("Building environment completed");

            productOperation.addLog("Installing cluster...");
            ClusterSetupStrategy s = manager.getClusterSetupStrategy(env, config, productOperation);
            s.setup();
            productOperation.addLog("Installing cluster completed");
            productOperation.addLogDone(null);

        } catch(EnvironmentBuildException ex) {
            String m = "Failed to build environment";
            productOperation.addLogFailed(m);
            manager.getLogger().error(m, ex);
        } catch(ClusterSetupException ex) {
            String m = "Failed to setup cluster";
            productOperation.addLog(ex.getMessage());
            productOperation.addLogFailed(m);
            manager.getLogger().error(m, ex);
        } finally {
            boolean b = productOperation.getState() != ProductOperationState.SUCCEEDED;
            if(b) destroyNodes(env);
        }
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

        try {
            manager.getPluginDao().saveInfo(StormConfig.PRODUCT_NAME,
                    clusterName, config);
            po.addLog("Cluster info successfully saved");
        } catch(DBException ex) {
            String m = "Failed to save cluster info";
            po.addLogFailed(m);
            manager.getLogger().error(m, ex);
        }

        if(configure(config))
            po.addLogDone("Storm cluster successfully configured");
        else
            po.addLogFailed("Failed to configure Storm cluster");
    }

    void destroyNodes(Environment env) {

        if(env == null || env.getNodes().isEmpty()) return;

        Set<Agent> set = new HashSet<>(env.getNodes().size());
        for(Node n : env.getNodes()) set.add(n.getAgent());
        productOperation.addLog("Destroying node(s)...");
        try {
            manager.getContainerManager().clonesDestroy(set);
            productOperation.addLog("Destroying node(s) completed");
        } catch(LxcDestroyException ex) {
            String m = "Failed to destroy node(s)";
            productOperation.addLog(m);
            manager.getLogger().error(m, ex);
        }

    }

    private boolean prepareNodes(StormConfig config) throws LxcCreateException {
        ProductOperation po = productOperation;
        Helper helper = new Helper(manager);
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
