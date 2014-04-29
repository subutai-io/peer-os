/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.api.spark.Spark;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dilshat
 */
public class SparkImpl implements Spark {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;

    public SparkImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;

        Commands.init(commandRunner);
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    public UUID installCluster(final Config config) {
        Preconditions.checkNotNull(config, "Configuration is null");

        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Installing cluster %s", config.getClusterName()));

        executor.execute(new Runnable() {

            public void run() {
                if (Strings.isNullOrEmpty(config.getClusterName()) || Util.isCollectionEmpty(config.getSlaveNodes()) || config.getMasterNode() == null) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null) {
                    po.addLogFailed("Master node is not connected\nInstallation aborted");
                    return;
                }

                //check if node agent is connected
                for (Iterator<Agent> it = config.getSlaveNodes().iterator(); it.hasNext(); ) {
                    Agent node = it.next();
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLog(String.format("Node %s is not connected. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getSlaveNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation\nInstallation aborted");
                    return;
                }

                po.addLog("Checking prerequisites...");

                //check installed ksks packages
                Set<Agent> allNodes = config.getAllNodes();
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(allNodes);
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }
                for (Iterator<Agent> it = allNodes.iterator(); it.hasNext(); ) {
                    Agent node = it.next();

                    AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());
                    if (result.getStdOut().contains("ksks-spark")) {
                        po.addLog(String.format("Node %s already has Spark installed. Omitting this node from installation", node.getHostname()));
                        config.getSlaveNodes().remove(node);
                        it.remove();
                    } else if (!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation", node.getHostname()));
                        config.getSlaveNodes().remove(node);
                        it.remove();
                    }
                }

                if (config.getSlaveNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation\nInstallation aborted");
                    return;
                }
                if (!allNodes.contains(config.getMasterNode())) {
                    po.addLogFailed("Master node was omitted\nInstallation aborted");
                    return;
                }

                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Spark...");
                    //install spark            
                    Command installCommand = Commands.getInstallCommand(config.getAllNodes());
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded\nSetting master IP...");

                        Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getMasterNode(), config.getAllNodes());
                        commandRunner.runCommand(setMasterIPCommand);

                        if (setMasterIPCommand.hasSucceeded()) {
                            po.addLog("Setting master IP succeeded\nRegistering slaves...");

                            Command addSlavesCommand = Commands.getAddSlavesCommand(config.getSlaveNodes(), config.getMasterNode());
                            commandRunner.runCommand(addSlavesCommand);

                            if (addSlavesCommand.hasSucceeded()) {
                                po.addLog("Slaves successfully registered\nStarting cluster...");

                                Command startNodesCommand = Commands.getStartAllCommand(config.getMasterNode());
                                final AtomicInteger okCount = new AtomicInteger(0);
                                commandRunner.runCommand(startNodesCommand, new CommandCallback() {

                                    @Override
                                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                                        okCount.set(Util.countNumberOfOccurences(agentResult.getStdOut(), "starting"));

                                        if (okCount.get() >= config.getAllNodes().size()) {
                                            stop();
                                        }
                                    }

                                });

                                if (okCount.get() >= config.getAllNodes().size()) {
                                    po.addLogDone("cluster started successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to start cluster, %s", startNodesCommand.getAllErrors()));
                                }

                            } else {
                                po.addLogFailed(String.format("Failed to register slaves with master, %s", addSlavesCommand.getAllErrors()));
                            }
                        } else {
                            po.addLogFailed(String.format("Setting master IP failed, %s", setMasterIPCommand.getAllErrors()));
                        }

                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                    }
                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
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

                for (Agent node : config.getAllNodes()) {
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                        return;
                    }
                }

                po.addLog("Uninstalling Spark...");

                Command uninstallCommand = Commands.getUninstallCommand(config.getAllNodes());
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    for (AgentResult result : uninstallCommand.getResults().values()) {
                        Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-spark is not installed, so not removed")) {
                                po.addLog(String.format("Spark is not installed, so not removed on node %s",
                                        agent == null ? result.getAgentUUID() : agent.getHostname()));
                            } else {
                                po.addLog(String.format("Spark is removed from node %s",
                                        agent == null ? result.getAgentUUID() : agent.getHostname()));
                            }
                        } else {
                            po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                                    agent == null ? result.getAgentUUID() : agent.getHostname()));
                        }
                    }
                    po.addLog("Updating db...");
                    if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                        po.addLogDone("Cluster info deleted from DB\nDone");
                    } else {
                        po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed(String.format("Uninstallation failed, %s", uninstallCommand.getAllErrors()));
                }
            }
        });

        return po.getId();
    }

    public UUID addSlaveNode(final String clusterName, final String lxcHostname) {

        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node %s to %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname()));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format("New node %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                //check if node is in the cluster
                if (config.getSlaveNodes().contains(agent)) {
                    po.addLogFailed(String.format("Node %s already belongs to this cluster\nOperation aborted", agent.getHostname()));
                    return;
                }

                po.addLog("Checking prerequisites...");

                boolean install = !agent.equals(config.getMasterNode());

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
                    return;
                }

                AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

                if (result.getStdOut().contains("ksks-spark") && install) {
                    po.addLogFailed(String.format("Node %s already has Spark installed\nOperation aborted", lxcHostname));
                    return;
                } else if (!result.getStdOut().contains("ksks-hadoop")) {
                    po.addLogFailed(String.format("Node %s has no Hadoop installation\nOperation aborted", lxcHostname));
                    return;
                }

                config.getSlaveNodes().add(agent);
                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info updated in DB");
                    //install spark            

                    if (install) {
                        po.addLog("Installing Spark...");
                        Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(agent));
                        commandRunner.runCommand(installCommand);

                        if (installCommand.hasSucceeded()) {
                            po.addLog("Installation succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                            return;
                        }
                    }

                    po.addLog("Setting master IP on slave...");
                    Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getMasterNode(), Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(setMasterIPCommand);

                    if (setMasterIPCommand.hasSucceeded()) {
                        po.addLog("Master IP successfully set\nRegistering slave with master...");

                        Command addSlaveCommand = Commands.getAddSlaveCommand(agent, config.getMasterNode());
                        commandRunner.runCommand(addSlaveCommand);

                        if (addSlaveCommand.hasSucceeded()) {
                            po.addLog("Registration succeeded\nRestarting master...");

                            Command restartMasterCommand = Commands.getRestartMasterCommand(config.getMasterNode());
                            final AtomicBoolean ok = new AtomicBoolean();
                            commandRunner.runCommand(restartMasterCommand, new CommandCallback() {

                                @Override
                                public void onResponse(Response response, AgentResult agentResult, Command command) {
                                    if (agentResult.getStdOut().contains("starting")) {
                                        ok.set(true);
                                        stop();
                                    }
                                }

                            });

                            if (ok.get()) {
                                po.addLog("Master restarted successfully\nStarting Spark on new node...");

                                Command startSlaveCommand = Commands.getStartSlaveCommand(agent);
                                ok.set(false);
                                commandRunner.runCommand(startSlaveCommand, new CommandCallback() {

                                    @Override
                                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                                        if (agentResult.getStdOut().contains("starting")) {
                                            ok.set(true);
                                            stop();
                                        }
                                    }

                                });

                                if (ok.get()) {
                                    po.addLogDone("Spark started successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to start Spark, %s", startSlaveCommand.getAllErrors()));
                                }

                            } else {
                                po.addLogFailed(String.format("Master restart failed, %s", restartMasterCommand.getAllErrors()));
                            }

                        } else {
                            po.addLogFailed(String.format("Registration failed, %s", addSlaveCommand.getAllErrors()));
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to set master IP, %s", setMasterIPCommand.getAllErrors()));
                    }

                } else {
                    po.addLogFailed("Could not update cluster info in DB! Please see logs\nOperation aborted");
                }
            }
        });

        return po.getId();
    }

    public UUID destroySlaveNode(final String clusterName, final String lxcHostname) {

        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                if (config.getSlaveNodes().size() == 1) {
                    po.addLogFailed("This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                //check if node is in the cluster
                if (!config.getSlaveNodes().contains(agent)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", agent.getHostname()));
                    return;
                }

                po.addLog("Unregistering slave from master...");

                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) != null) {

                    Command clearSlavesCommand = Commands.getClearSlaveCommand(agent, config.getMasterNode());
                    commandRunner.runCommand(clearSlavesCommand);

                    if (clearSlavesCommand.hasSucceeded()) {
                        po.addLog("Successfully unregistered slave from master\nRestarting master...");

                        Command restartMasterCommand = Commands.getRestartMasterCommand(config.getMasterNode());
                        final AtomicBoolean ok = new AtomicBoolean();
                        commandRunner.runCommand(restartMasterCommand, new CommandCallback() {

                            @Override
                            public void onResponse(Response response, AgentResult agentResult, Command command) {
                                if (agentResult.getStdOut().contains("starting")) {
                                    ok.set(true);
                                    stop();
                                }
                            }

                        });

                        if (ok.get()) {
                            po.addLog("Master restarted successfully");
                        } else {
                            po.addLog(String.format("Master restart failed, %s, skipping...", restartMasterCommand.getAllErrors()));
                        }
                    } else {
                        po.addLog(String.format("Failed to unregister slave from master: %s, skipping...",
                                clearSlavesCommand.getAllErrors()));
                    }
                } else {
                    po.addLog("Failed to unregister slave from master: Master is not connected, skipping...");
                }

                boolean uninstall = !agent.equals(config.getMasterNode());

                if (uninstall) {
                    po.addLog("Uninstalling Spark...");

                    Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(uninstallCommand);

                    if (uninstallCommand.hasCompleted()) {
                        AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-spark is not installed, so not removed")) {
                                po.addLog(String.format("Spark is not installed, so not removed on node %s",
                                        agent.getHostname()));
                            } else {
                                po.addLog(String.format("Spark is removed from node %s",
                                        agent.getHostname()));
                            }
                        } else {
                            po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                                    agent.getHostname()));
                        }

                    } else {
                        po.addLogFailed(String.format("Uninstallation failed, %s", uninstallCommand.getAllErrors()));
                        return;
                    }
                } else {
                    po.addLog("Stopping slave...");

                    Command stopSlaveCommand = Commands.getStopSlaveCommand(agent);
                    commandRunner.runCommand(stopSlaveCommand);

                    if (stopSlaveCommand.hasSucceeded()) {
                        po.addLog("Slave stopped successfully");
                    } else {
                        po.addLog(String.format("Failed to stop slave, %s, skipping...", stopSlaveCommand.getAllErrors()));
                    }
                }

                config.getSlaveNodes().remove(agent);
                po.addLog("Updating db...");

                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLogDone("Cluster info updated in DB\nDone");
                } else {
                    po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                }

            }
        });

        return po.getId();
    }

    public UUID changeMasterNode(final String clusterName, final String newMasterHostname, final boolean keepSlave) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Changing master to %s in %s", newMasterHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname()));
                    return;
                }

                Agent newMaster = agentManager.getAgentByHostname(newMasterHostname);
                if (newMaster == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", newMasterHostname));
                    return;
                }

                if (newMaster.equals(config.getMasterNode())) {
                    po.addLogFailed(String.format("Node %s is already a master node\nOperation aborted", newMasterHostname));
                    return;
                }

                //check if node is in the cluster
                if (!config.getAllNodes().contains(newMaster)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", newMasterHostname));
                    return;
                }

                po.addLog("Stopping all nodes...");
                //stop all nodes
                Command stopNodesCommand = Commands.getStopAllCommand(config.getMasterNode());
                commandRunner.runCommand(stopNodesCommand);
                if (stopNodesCommand.hasSucceeded()) {
                    po.addLog("All nodes stopped\nClearing slaves on old master...");
                    //clear slaves from old master
                    Command clearSlavesCommand = Commands.getClearSlavesCommand(config.getMasterNode());
                    commandRunner.runCommand(clearSlavesCommand);
                    if (clearSlavesCommand.hasSucceeded()) {
                        po.addLog("Slaves cleared successfully");
                    } else {
                        po.addLog(String.format("Clearing slaves failed, %s, skipping...", clearSlavesCommand.getAllErrors()));
                    }
                    //add slaves to new master, if keepSlave=true then master node is also added as slave
                    config.getSlaveNodes().add(config.getMasterNode());
                    config.setMasterNode(newMaster);
                    if (keepSlave) {
                        config.getSlaveNodes().add(newMaster);
                    } else {
                        config.getSlaveNodes().remove(newMaster);
                    }
                    po.addLog("Adding nodes to new master...");
                    Command addSlavesCommand = Commands.getAddSlavesCommand(config.getSlaveNodes(), config.getMasterNode());
                    commandRunner.runCommand(addSlavesCommand);
                    if (addSlavesCommand.hasSucceeded()) {
                        po.addLog("Nodes added successfully\nSetting new master IP...");
                        //modify master ip on all nodes
                        Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getMasterNode(), config.getAllNodes());
                        commandRunner.runCommand(setMasterIPCommand);
                        if (setMasterIPCommand.hasSucceeded()) {
                            po.addLog("Master IP set successfully\nStarting cluster...");
                            //start master & slaves

                            Command startNodesCommand = Commands.getStartAllCommand(config.getMasterNode());
                            final AtomicInteger okCount = new AtomicInteger(0);
                            commandRunner.runCommand(startNodesCommand, new CommandCallback() {

                                @Override
                                public void onResponse(Response response, AgentResult agentResult, Command command) {
                                    okCount.set(Util.countNumberOfOccurences(agentResult.getStdOut(), "starting"));

                                    if (okCount.get() >= config.getAllNodes().size()) {
                                        stop();
                                    }

                                }

                            });

                            if (okCount.get() >= config.getAllNodes().size()) {
                                po.addLog("Cluster started successfully");
                            } else {
                                po.addLog(String.format("Start of cluster failed, %s, skipping...", startNodesCommand.getAllErrors()));
                            }

                            po.addLog("Updating db...");
                            //update db
                            if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                                po.addLogDone("Cluster info updated in DB\nDone");
                            } else {
                                po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                            }
                        } else {
                            po.addLogFailed(String.format("Failed to set master IP on all nodes, %s\nOperation aborted", setMasterIPCommand.getAllErrors()));
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to add slaves to new master, %s\nOperation aborted", addSlavesCommand.getAllErrors()));
                    }

                } else {
                    po.addLogFailed(String.format("Failed to stop all nodes, %s", stopNodesCommand.getAllErrors()));
                }
            }
        });

        return po.getId();
    }

    public UUID startNode(final String clusterName, final String lxcHostname, final boolean master) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }

                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
                    return;
                }

                if (master && !config.getMasterNode().equals(node)) {
                    po.addLogFailed(String.format("Node %s is not a master node\nOperation aborted", node.getHostname()));
                    return;
                } else if (!master && !config.getSlaveNodes().contains(node)) {
                    po.addLogFailed(String.format("Node %s is not a slave node\nOperation aborted", node.getHostname()));
                    return;
                }

                po.addLog(String.format("Starting %s on %s...", master ? "master" : "slave", node.getHostname()));

                Command startCommand;
                if (master) {
                    startCommand = Commands.getStartMasterCommand(node);
                } else {
                    startCommand = Commands.getStartSlaveCommand(node);
                }

                final AtomicBoolean ok = new AtomicBoolean();
                commandRunner.runCommand(startCommand, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains("starting")) {
                            ok.set(true);
                            stop();
                        }
                    }

                });

                if (ok.get()) {
                    po.addLogDone(String.format("Node %s started", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Starting node %s failed, %s", node.getHostname(), startCommand.getAllErrors()));
                }

            }
        });

        return po.getId();

    }

    public UUID stopNode(final String clusterName, final String lxcHostname, final boolean master) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }

                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
                    return;
                }

                if (master && !config.getMasterNode().equals(node)) {
                    po.addLogFailed(String.format("Node %s is not a master node\nOperation aborted", node.getHostname()));
                    return;
                } else if (!master && !config.getSlaveNodes().contains(node)) {
                    po.addLogFailed(String.format("Node %s is not a slave node\nOperation aborted", node.getHostname()));
                    return;
                }

                po.addLog(String.format("Stopping %s on %s...", master ? "master" : "slave", node.getHostname()));

                Command stopCommand;
                if (master) {
                    stopCommand = Commands.getStopMasterCommand(node);
                } else {
                    stopCommand = Commands.getStopSlaveCommand(node);
                }
                commandRunner.runCommand(stopCommand);

                if (stopCommand.hasSucceeded()) {
                    po.addLogDone(String.format("Node %s stopped", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Stopping %s failed, %s", node.getHostname(), stopCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking state of %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }

                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
                    return;
                }

                po.addLog("Checking node...");

                Command checkNodeCommand = Commands.getStatusAllCommand(node);
                commandRunner.runCommand(checkNodeCommand);

                AgentResult res = checkNodeCommand.getResults().get(node.getUuid());
                if (checkNodeCommand.hasSucceeded()) {
                    po.addLogDone(String.format("%s", res.getStdOut()));
                } else {
                    po.addLogFailed(String.format("Faied to check status, %s", checkNodeCommand.getAllErrors()));
                }
            }
        });

        return po.getId();
    }

}
