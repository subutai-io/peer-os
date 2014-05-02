/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.shark;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.shark.Config;
import org.safehaus.kiskis.mgmt.api.shark.Shark;
import org.safehaus.kiskis.mgmt.api.spark.Spark;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SharkImpl implements Shark {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Spark sparkManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;

    public SharkImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker, Spark sparkManager) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.sparkManager = sparkManager;

        Commands.init(commandRunner);
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Installing %s", Config.PRODUCT_KEY));

        executor.execute(new Runnable() {

            public void run() {
                if (config == null || Strings.isNullOrEmpty(config.getClusterName())) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                if (getCluster(config.getClusterName()) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                org.safehaus.kiskis.mgmt.api.spark.Config sparkConfig
                        = sparkManager.getCluster(config.getClusterName());
                if (sparkConfig == null) {
                    po.addLogFailed(String.format("Spark cluster '%s' not found\nInstallation aborted", config.getClusterName()));
                    return;
                }

                Config config = new Config();
                config.setClusterName(config.getClusterName());
                config.setNodes(sparkConfig.getAllNodes());

                for (Agent node : config.getNodes()) {
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLogFailed(String.format("Node %s is not connected\nInstallation aborted", node.getHostname()));
                        return;
                    }
                }

                po.addLog("Checking prerequisites...");

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getNodes());
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                for (Agent node : config.getNodes()) {
                    AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

                    if (result.getStdOut().contains("ksks-shark")) {
                        po.addLogFailed(String.format("Node %s already has Shark installed\nInstallation aborted", node.getHostname()));
                        return;
                    } else if (!result.getStdOut().contains("ksks-spark")) {
                        po.addLog(String.format("Node %s has no Spark installation\nInstallation aborted", node.getHostname()));
                        return;
                    }
                }

                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Shark...");

                    Command installCommand = Commands.getInstallCommand(config.getNodes());
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded\nSetting Master IP...");

                        Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getNodes(), sparkConfig.getMasterNode());
                        commandRunner.runCommand(setMasterIPCommand);

                        if (setMasterIPCommand.hasSucceeded()) {
                            po.addLogDone("Master IP successfully set\nDone");
                        } else {
                            po.addLogFailed(String.format("Failed to set Master IP, %s", setMasterIPCommand.getAllErrors()));
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
                Config config = getCluster(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                for (Agent node : config.getNodes()) {
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                        return;
                    }
                }

                po.addLog("Uninstalling Shark...");

                Command uninstallCommand = Commands.getUninstallCommand(config.getNodes());
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    for (AgentResult result : uninstallCommand.getResults().values()) {
                        Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-shark is not installed, so not removed")) {
                                po.addLog(String.format("Shark is not installed, so not removed on node %s",
                                        agent == null ? result.getAgentUUID() : agent.getHostname()));
                            } else {
                                po.addLog(String.format("Shark is removed from node %s",
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
                    po.addLogFailed("Uninstallation failed, command timed out");
                }

            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = getCluster(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                if (!config.getNodes().contains(agent)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
                    return;
                }

                if (config.getNodes().size() == 1) {
                    po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }
                po.addLog("Uninstalling Shark...");
                Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
                    if (result.getExitCode() != null && result.getExitCode() == 0) {
                        if (result.getStdOut().contains("Package ksks-shark is not installed, so not removed")) {
                            po.addLog(String.format("Shark is not installed, so not removed on node %s",
                                    agent.getHostname()));
                        } else {
                            po.addLog(String.format("Shark is removed from node %s",
                                    agent.getHostname()));
                        }
                    } else {
                        po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                                agent.getHostname()));
                    }

                    config.getNodes().remove(agent);
                    po.addLog("Updating db...");

                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLogDone("Cluster info update in DB\nDone");
                    } else {
                        po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed("Uninstallation failed, command timed out");
                }
            }
        });

        return po.getId();
    }

    public UUID addNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getCluster(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                //check if node agent is connected
                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                if (config.getNodes().contains(agent)) {
                    po.addLogFailed(String.format("Node %s already belongs to this cluster\nOperation aborted", lxcHostname));
                    return;
                }

                org.safehaus.kiskis.mgmt.api.spark.Config sparkConfig
                        = sparkManager.getCluster(clusterName);
                if (sparkConfig == null) {
                    po.addLogFailed(String.format("Spark cluster '%s' not found\nInstallation aborted", clusterName));
                    return;
                }

                if (!sparkConfig.getAllNodes().contains(agent)) {
                    po.addLogFailed(String.format("Node %s does not belong to %s spark cluster\nOperation aborted", lxcHostname, clusterName));
                    return;
                }

                po.addLog("Checking prerequisites...");

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

                if (result.getStdOut().contains("ksks-shark")) {
                    po.addLogFailed(String.format("Node %s already has Shark installed\nInstallation aborted", lxcHostname));
                    return;
                } else if (!result.getStdOut().contains("ksks-spark")) {
                    po.addLogFailed(String.format("Node %s has no Spark installation\nInstallation aborted", lxcHostname));
                    return;
                }

                config.getNodes().add(agent);
                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info updated in DB\nInstalling Shark...");

                    Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded\nSetting Master IP...");

                        Command setMasterIPCommand = Commands.getSetMasterIPCommand(Util.wrapAgentToSet(agent), sparkConfig.getMasterNode());
                        commandRunner.runCommand(setMasterIPCommand);

                        if (setMasterIPCommand.hasSucceeded()) {
                            po.addLogDone("Master IP set successfully\nDone");
                        } else {
                            po.addLogFailed(String.format("Failed to set Master IP, %s", setMasterIPCommand.getAllErrors()));
                        }
                    } else {

                        po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                    }
                } else {
                    po.addLogFailed("Could not update cluster info in DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

    public UUID actualizeMasterIP(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Actualizing master IP of %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getCluster(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                org.safehaus.kiskis.mgmt.api.spark.Config sparkConfig
                        = sparkManager.getCluster(clusterName);
                if (sparkConfig == null) {
                    po.addLogFailed(String.format("Spark cluster '%s' not found\nInstallation aborted", clusterName));
                    return;
                }

                for (Agent node : config.getNodes()) {
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                        return;
                    }
                }

                Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getNodes(), sparkConfig.getMasterNode());
                commandRunner.runCommand(setMasterIPCommand);

                if (setMasterIPCommand.hasSucceeded()) {
                    po.addLogDone("Master IP actualized successfully\nDone");
                } else {
                    po.addLogFailed(String.format("Failed to actualize Master IP, %s", setMasterIPCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

}
