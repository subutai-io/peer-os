package org.safehaus.kiskis.mgmt.impl.zookeeper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.api.zookeeper.Api;
import org.safehaus.kiskis.mgmt.api.zookeeper.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Impl implements Api {

    private static CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        Impl.commandRunner = commandRunner;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        commandRunner = null;
        executor.shutdown();
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public UUID installCluster(final Config config) {
        Preconditions.checkNotNull(config, "Configuration is null");

        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, String.format("Installing %s", config.getClusterName()));

        executor.execute(new Runnable() {

            public void run() {

                if (Strings.isNullOrEmpty(config.getZkName()) || Strings.isNullOrEmpty(config.getClusterName()) || config.getNumberOfNodes() <= 0) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d lxc containers...", config.getNumberOfNodes()));
                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(config.getNumberOfNodes());
                    config.setNodes(new HashSet<Agent>());

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        config.getNodes().addAll(entry.getValue());
                    }
                    po.addLog("Lxc containers created successfully\nUpdating db...");
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

                        po.addLog(String.format("Cluster info saved to DB\nInstalling %s...", Config.PRODUCT_KEY));

                        //install
                        Command installCommand = Commands.getInstallCommand(config.getNodes());
                        commandRunner.runCommand(installCommand);

                        if (installCommand.hasSucceeded()) {
                            po.addLog("Installation succeeded\nUpdating settings...");

                            //update settings
                            Command updateSettingsCommand = Commands.getUpdateSettingsCommand(config.getZkName(), config.getNodes());
                            commandRunner.runCommand(updateSettingsCommand);

                            if (updateSettingsCommand.hasSucceeded()) {

                                po.addLog(String.format("Settings updated\nStarting %s...", Config.PRODUCT_KEY));
                                //start all nodes
                                Command startCommand = Commands.getStartCommand(config.getNodes());
                                final AtomicInteger count = new AtomicInteger();
                                commandRunner.runCommand(startCommand, new CommandCallback() {
                                    @Override
                                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                                        if (agentResult.getStdOut().contains("STARTED")) {
                                            if (count.incrementAndGet() == config.getNodes().size()) {
                                                stop();
                                            }
                                        }
                                    }
                                });

                                if (count.get() == config.getNodes().size()) {
                                    po.addLogDone(String.format("Starting %s succeeded\nDone", Config.PRODUCT_KEY));
                                } else {
                                    po.addLogFailed(String.format("Starting %s failed, %s", Config.PRODUCT_KEY, startCommand.getAllErrors()));
                                }
                            } else {
                                po.addLogFailed(String.format(
                                        "Failed to update settings, %s\nPlease update settings manually and restart the cluster",
                                        updateSettingsCommand.getAllErrors()));
                            }
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                        }

                    } else {
                        //destroy all lxcs also
                        try {
                            lxcManager.destroyLxcs(lxcAgentsMap);
                        } catch (LxcDestroyException ex) {
                            po.addLogFailed("Could not save cluster info to DB! Please see logs. Use LXC module to cleanup\nInstallation aborted");
                        }
                        po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                    }
                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                po.addLog("Destroying lxc containers...");

                try {
                    lxcManager.destroyLxcs(config.getNodes());
                    po.addLog("Lxc containers successfully destroyed");
                } catch (LxcDestroyException ex) {
                    po.addLog(String.format("%s, skipping...", ex.getMessage()));
                }
                po.addLog("Updating db...");
                if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                    po.addLogDone("Cluster info deleted from DB\nDone");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                }

            }
        });

        return po.getId();
    }

    public UUID startNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }

                if (!config.getNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostName, clusterName));
                    return;
                }

                po.addLog("Starting node...");

                Command startCommand = Commands.getStartCommand(Util.wrapAgentToSet(node));
                final AtomicBoolean ok = new AtomicBoolean();
                commandRunner.runCommand(startCommand, new CommandCallback() {
                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains("STARTED")) {
                            ok.set(true);
                            stop();
                        }
                    }
                });

                if (ok.get()) {
                    po.addLogDone(String.format("Node on %s started", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to start node %s. %s",
                            lxcHostName, startCommand.getAllErrors()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID stopNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }
                if (!config.getNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostName, clusterName));
                    return;
                }
                po.addLog("Stopping node...");

                Command stopCommand = Commands.getStopCommand(node);
                commandRunner.runCommand(stopCommand);
                NodeState state = NodeState.UNKNOWN;
                if (stopCommand.hasCompleted()) {
                    AgentResult result = stopCommand.getResults().get(node.getUuid());
                    if (result.getStdOut().contains("STOPPED")) {
                        state = NodeState.STOPPED;
                    }
                }

                if (NodeState.STOPPED.equals(state)) {
                    po.addLogDone(String.format("Node on %s stopped", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            lxcHostName, stopCommand.getAllErrors()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }
                if (!config.getNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostName, clusterName));
                    return;
                }
                po.addLog("Checking node...");
                Command checkCommand = Commands.getStatusCommand(node);
                commandRunner.runCommand(checkCommand);
                NodeState state = NodeState.UNKNOWN;
                if (checkCommand.hasCompleted()) {
                    AgentResult result = checkCommand.getResults().get(node.getUuid());
                    if (result.getStdOut().contains("is Running")) {
                        state = NodeState.RUNNING;
                    } else if (result.getStdOut().contains("is NOT Running")) {
                        state = NodeState.STOPPED;
                    }
                }

                if (NodeState.UNKNOWN.equals(state)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            lxcHostName,
                            checkCommand.getAllErrors()
                    ));
                } else {
                    po.addLogDone(String.format("Node %s is %s",
                            lxcHostName,
                            state
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostName);
                if (agent == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }
                if (!config.getNodes().contains(agent)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostName, clusterName));
                    return;
                }

                if (config.getNodes().size() == 1) {
                    po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                //destroy lxc
                po.addLog("Destroying lxc container...");
                Agent physicalAgent = agentManager.getAgentByHostname(agent.getParentHostName());
                if (physicalAgent == null) {
                    po.addLog(
                            String.format("Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                                    agent.getHostname())
                    );
                } else {
                    if (!lxcManager.destroyLxcOnHost(physicalAgent, agent.getHostname())) {
                        po.addLog("Could not destroy lxc container. Use LXC module to cleanup, skipping...");
                    } else {
                        po.addLog("Lxc container destroyed successfully");
                    }
                }

                config.getNodes().remove(agent);

                //update settings
                po.addLog("Updating settings...");
                Command updateSettingsCommand = Commands.getUpdateSettingsCommand(config.getZkName(), config.getNodes());
                commandRunner.runCommand(updateSettingsCommand);

                if (updateSettingsCommand.hasSucceeded()) {
                    po.addLog("Settings updated\nRestarting cluster...");
                    //restart all other nodes with new configuration
                    Command restartCommand = Commands.getRestartCommand(config.getNodes());
                    final AtomicInteger count = new AtomicInteger();
                    commandRunner.runCommand(restartCommand, new CommandCallback() {
                        @Override
                        public void onResponse(Response response, AgentResult agentResult, Command command) {
                            if (agentResult.getStdOut().contains("STARTED")) {
                                if (count.incrementAndGet() == config.getNodes().size()) {
                                    stop();
                                }
                            }
                        }
                    });

                    if (count.get() == config.getNodes().size()) {
                        po.addLog("Cluster successfully restarted");
                    } else {
                        po.addLog(String.format("Failed to restart cluster, %s, skipping...", restartCommand.getAllErrors()));
                    }
                } else {
                    po.addLog(
                            String.format(
                                    "Settings update failed, %s\nPlease update settings manually and restart the cluster, skipping...",
                                    updateSettingsCommand.getAllErrors())
                    );
                }

                //update db
                po.addLog("Updating db...");
                if (!dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
                            config.getClusterName()));
                } else {
                    po.addLogDone("Done");
                }
            }
        });

        return po.getId();
    }

    public UUID addNode(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                try {

                    //create lxc
                    po.addLog("Creating lxc container...");

                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(1);

                    Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

                    config.getNodes().add(lxcAgent);

                    po.addLog(String.format("Lxc container created successfully\nInstalling %s...", Config.PRODUCT_KEY));

                    //install
                    Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(lxcAgent));
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasCompleted()) {
                        po.addLog("Installation succeeded\nUpdating db...");
                        //update db
                        if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                            po.addLog("Cluster info updated in DB\nUpdating settings...");

                            //update settings
                            Command updateSettingsCommand = Commands.getUpdateSettingsCommand(config.getZkName(), config.getNodes());
                            commandRunner.runCommand(updateSettingsCommand);

                            if (updateSettingsCommand.hasSucceeded()) {
                                po.addLog("Settings updated\nRestarting cluster...");
                                //restart all nodes
                                Command restartCommand = Commands.getRestartCommand(config.getNodes());
                                final AtomicInteger count = new AtomicInteger();
                                commandRunner.runCommand(restartCommand, new CommandCallback() {
                                    @Override
                                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                                        if (agentResult.getStdOut().contains("STARTED")) {
                                            if (count.incrementAndGet() == config.getNodes().size()) {
                                                stop();
                                            }
                                        }
                                    }
                                });
                                if (count.get() == config.getNodes().size()) {
                                    po.addLogDone("Cluster restarted successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to restart cluster, %s", restartCommand.getAllErrors()));
                                }
                            } else {
                                po.addLogFailed(
                                        String.format(
                                                "Settings update failed, %s.\nPlease update settings manually and restart the cluster",
                                                updateSettingsCommand.getAllErrors())
                                );
                            }
                        } else {
                            po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                        }
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s\nUse LXC Module to cleanup",
                                installCommand.getAllErrors()));
                    }

                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }
            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {

        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

    }

}
