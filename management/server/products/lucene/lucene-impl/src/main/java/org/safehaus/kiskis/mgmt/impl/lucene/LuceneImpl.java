/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lucene;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lucene.Config;
import org.safehaus.kiskis.mgmt.api.lucene.Lucene;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;

/**
 * @author dilshat
 */
public class LuceneImpl implements Lucene {

    private static CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        commandRunner = null;
        executor.shutdown();
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        LuceneImpl.commandRunner = commandRunner;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Installing cluster %s", config.getClusterName()));

        executor.execute(new Runnable() {

            public void run() {
                if (config == null || Util.isStringEmpty(config.getClusterName()) || Util.isCollectionEmpty(config.getNodes())) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                //check if node agent is connected
                for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
                    Agent node = it.next();
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLog(String.format("Node %s is not connected. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                po.addLog("Checking prerequisites...");

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getNodes());
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
                    Agent node = it.next();

                    AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

                    if (result.getStdOut().contains("ksks-lucene")) {
                        po.addLog(String.format("Node %s already has Lucene installed. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    } else if (!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                //save to db
                po.addLog("Updating db...");
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Lucene...");

                    //install lucene            
                    Command installCommand = Commands.getInstallCommand(config.getNodes());
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLogDone("Installation succeeded\nDone");
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

                for (Agent node : config.getNodes()) {
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLogFailed(String.format("Node %s is not connected\nOperation aborted", node.getHostname()));
                        return;
                    }
                }

                po.addLog("Uninstalling Lucene...");

                Command uninstallCommand = Commands.getUninstallCommand(config.getNodes());
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    for (AgentResult result : uninstallCommand.getResults().values()) {
                        Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-lucene is not installed, so not removed")) {
                                po.addLog(String.format("Lucene is not installed, so not removed on node %s", result.getStdErr(),
                                        agent == null ? result.getAgentUUID() : agent.getHostname()));
                            } else {
                                po.addLog(String.format("Lucene is removed from node %s",
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

                if (!config.getNodes().contains(agent)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
                    return;
                }

                if (config.getNodes().size() == 1) {
                    po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }
                po.addLog("Uninstalling Lucene...");
                Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
                    if (result.getExitCode() != null && result.getExitCode() == 0) {
                        if (result.getStdOut().contains("Package ksks-lucene is not installed, so not removed")) {
                            po.addLog(String.format("Lucene is not installed, so not removed on node %s", result.getStdErr(),
                                    agent.getHostname()));
                        } else {
                            po.addLog(String.format("Lucene is removed from node %s",
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
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
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
                    po.addLogFailed(String.format("Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName));
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

                if (result.getStdOut().contains("ksks-lucene")) {
                    po.addLogFailed(String.format("Node %s already has Lucene installed\nInstallation aborted", lxcHostname));
                    return;
                } else if (!result.getStdOut().contains("ksks-hadoop")) {
                    po.addLogFailed(String.format("Node %s has no Hadoop installation\nInstallation aborted", lxcHostname));
                    return;
                }

                config.getNodes().add(agent);
                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info updated in DB\nInstalling Lucene...");
                    //install lucene            

                    Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLogDone("Installation succeeded\nDone");
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

}
