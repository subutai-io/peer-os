/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.presto;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.presto.Config;
import org.safehaus.kiskis.mgmt.api.presto.Presto;
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
public class PrestoImpl implements Presto {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;

    public PrestoImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
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
                if (Strings.isNullOrEmpty(config.getClusterName()) || Util.isCollectionEmpty(config.getWorkers()) || config.getCoordinatorNode() == null) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (agentManager.getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
                    po.addLogFailed("Coordinator node is not connected\nInstallation aborted");
                    return;
                }

                //check if node agent is connected
                for (Iterator<Agent> it = config.getWorkers().iterator(); it.hasNext(); ) {
                    Agent node = it.next();
                    if (agentManager.getAgentByHostname(node.getHostname()) == null) {
                        po.addLog(String.format("Node %s is not connected. Omitting this node from installation", node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getWorkers().isEmpty()) {
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
                    if (result.getStdOut().contains("ksks-presto")) {
                        po.addLog(String.format("Node %s already has Presto installed. Omitting this node from installation", node.getHostname()));
                        config.getWorkers().remove(node);
                        it.remove();
                    } else if (!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLog(String.format("Node %s has no Hadoop installation. Omitting this node from installation", node.getHostname()));
                        config.getWorkers().remove(node);
                        it.remove();
                    }
                }

                if (config.getWorkers().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation\nInstallation aborted");
                    return;
                }
                if (!allNodes.contains(config.getCoordinatorNode())) {
                    po.addLogFailed("Coordinator node was omitted\nInstallation aborted");
                    return;
                }

                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Presto...");
                    //install presto            

                    Command installCommand = Commands.getInstallCommand(config.getAllNodes());
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded\nConfiguring coordinator...");

                        Command configureCoordinatorCommand = Commands.getSetCoordinatorCommand(config.getCoordinatorNode());
                        commandRunner.runCommand(configureCoordinatorCommand);

                        if (configureCoordinatorCommand.hasSucceeded()) {
                            po.addLog("Coordinator configured successfully\nConfiguring workers...");

                            Command configureWorkersCommand = Commands.getSetWorkerCommand(config.getCoordinatorNode(), config.getWorkers());
                            commandRunner.runCommand(configureWorkersCommand);

                            if (configureWorkersCommand.hasSucceeded()) {
                                po.addLog("Workers configured successfully\nStarting Presto...");

                                Command startNodesCommand = Commands.getStartCommand(config.getAllNodes());
                                final AtomicInteger okCount = new AtomicInteger();
                                commandRunner.runCommand(startNodesCommand, new CommandCallback() {

                                    @Override
                                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                                        if (agentResult.getStdOut().contains("Started") && okCount.incrementAndGet() == config.getAllNodes().size()) {
                                            stop();
                                        }
                                    }

                                });

                                if (okCount.get() == config.getAllNodes().size()) {
                                    po.addLogDone("Presto started successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to start Presto, %s", startNodesCommand.getAllErrors()));
                                }

                            } else {
                                po.addLogFailed(String.format("Failed to configure workers, %s", configureWorkersCommand.getAllErrors()));
                            }
                        } else {
                            po.addLogFailed(String.format("Failed to configure coordinator, %s", configureCoordinatorCommand.getAllErrors()));
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

                po.addLog("Uninstalling Presto...");

                Command uninstallCommand = Commands.getUninstallCommand(config.getAllNodes());
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    for (AgentResult result : uninstallCommand.getResults().values()) {
                        Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-presto is not installed, so not removed")) {
                                po.addLog(String.format("Presto is not installed, so not removed on node %s",
                                        agent == null ? result.getAgentUUID() : agent.getHostname()));
                            } else {
                                po.addLog(String.format("Presto is removed from node %s",
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

    public UUID addWorkerNode(final String clusterName, final String lxcHostname) {

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

                if (agentManager.getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Coordinator node %s is not connected\nOperation aborted", config.getCoordinatorNode().getHostname()));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format("New node %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                //check if node is in the cluster
                if (config.getWorkers().contains(agent)) {
                    po.addLogFailed(String.format("Node %s already belongs to this cluster\nOperation aborted", agent.getHostname()));
                    return;
                }

                po.addLog("Checking prerequisites...");

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
                    return;
                }

                AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

                if (result.getStdOut().contains("ksks-presto")) {
                    po.addLogFailed(String.format("Node %s already has Presto installed\nOperation aborted", lxcHostname));
                    return;
                } else if (!result.getStdOut().contains("ksks-hadoop")) {
                    po.addLogFailed(String.format("Node %s has no Hadoop installation\nOperation aborted", lxcHostname));
                    return;
                }

                config.getWorkers().add(agent);
                po.addLog("Updating db...");
                //save to db
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info updated in DB");
                    //install presto            

                    po.addLog("Installing Presto...");
                    Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                        return;
                    }

                    po.addLog("Configuring worker...");
                    Command configureWorkerCommand = Commands.getSetWorkerCommand(config.getCoordinatorNode(), Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(configureWorkerCommand);

                    if (configureWorkerCommand.hasSucceeded()) {
                        po.addLog("Worker configured successfully\nStarting Presto on new node...");

                        Command startCommand = Commands.getStartCommand(Util.wrapAgentToSet(agent));
                        final AtomicBoolean ok = new AtomicBoolean();
                        commandRunner.runCommand(startCommand, new CommandCallback() {

                            @Override
                            public void onResponse(Response response, AgentResult agentResult, Command command) {
                                if (agentResult.getStdOut().contains("Started")) {
                                    ok.set(true);
                                    stop();
                                }
                            }

                        });

                        if (ok.get()) {
                            po.addLogDone("Presto started successfully\nDone");
                        } else {
                            po.addLogFailed(String.format("Failed to start Presto, %s", startCommand.getAllErrors()));
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to configure worker, %s", configureWorkerCommand.getAllErrors()));
                    }

                } else {
                    po.addLogFailed("Could not update cluster info in DB! Please see logs\nOperation aborted");
                }
            }
        });

        return po.getId();
    }

    public UUID destroyWorkerNode(final String clusterName, final String lxcHostname) {

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

                if (config.getWorkers().size() == 1) {
                    po.addLogFailed("This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                //check if node is in the cluster
                if (!config.getWorkers().contains(agent)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", agent.getHostname()));
                    return;
                }

                po.addLog("Uninstalling Presto...");

                Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
                    if (result.getExitCode() != null && result.getExitCode() == 0) {
                        if (result.getStdOut().contains("Package ksks-presto is not installed, so not removed")) {
                            po.addLog(String.format("Presto is not installed, so not removed on node %s",
                                    agent.getHostname()));
                        } else {
                            po.addLog(String.format("Presto is removed from node %s",
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

                config.getWorkers().remove(agent);
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

    public UUID changeCoordinatorNode(final String clusterName, final String newCoordinatorHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Changing coordinator to %s in %s", newCoordinatorHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                if (agentManager.getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Coordinator %s is not connected\nOperation aborted", config.getCoordinatorNode().getHostname()));
                    return;
                }

                Agent newCoordinator = agentManager.getAgentByHostname(newCoordinatorHostname);
                if (newCoordinator == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", newCoordinatorHostname));
                    return;
                }

                if (newCoordinator.equals(config.getCoordinatorNode())) {
                    po.addLogFailed(String.format("Node %s is already a coordinator node\nOperation aborted", newCoordinatorHostname));
                    return;
                }

                //check if node is in the cluster
                if (!config.getWorkers().contains(newCoordinator)) {
                    po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", newCoordinatorHostname));
                    return;
                }

                po.addLog("Stopping all nodes...");
                //stop all nodes
                Command stopNodesCommand = Commands.getStopCommand(config.getAllNodes());
                commandRunner.runCommand(stopNodesCommand);

                if (stopNodesCommand.hasSucceeded()) {
                    po.addLog("All nodes stopped\nConfiguring coordinator...");

                    Command configureCoordinatorCommand = Commands.getSetCoordinatorCommand(newCoordinator);
                    commandRunner.runCommand(configureCoordinatorCommand);

                    if (configureCoordinatorCommand.hasSucceeded()) {
                        po.addLog("Coordinator configured successfully");
                    } else {
                        po.addLogFailed(String.format("Failed to configure coordinator, %s\nOperation aborted", configureCoordinatorCommand.getAllErrors()));
                        return;
                    }

                    config.getWorkers().add(config.getCoordinatorNode());
                    config.getWorkers().remove(newCoordinator);
                    config.setCoordinatorNode(newCoordinator);

                    po.addLog("Configuring workers...");

                    Command configureWorkersCommand = Commands.getSetWorkerCommand(newCoordinator, config.getWorkers());
                    commandRunner.runCommand(configureWorkersCommand);

                    if (configureWorkersCommand.hasSucceeded()) {
                        po.addLog("Workers configured successfully\nStarting cluster...");

                        Command startNodesCommand = Commands.getStartCommand(config.getAllNodes());
                        final AtomicInteger okCount = new AtomicInteger();
                        commandRunner.runCommand(startNodesCommand, new CommandCallback() {

                            @Override
                            public void onResponse(Response response, AgentResult agentResult, Command command) {
                                if (agentResult.getStdOut().contains("Started") && okCount.incrementAndGet() == config.getAllNodes().size()) {
                                    stop();
                                }
                            }

                        });

                        if (okCount.get() == config.getAllNodes().size()) {
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
                        po.addLogFailed(String.format("Failed to configure workers, %s\nOperation aborted", configureWorkersCommand.getAllErrors()));
                    }

                } else {
                    po.addLogFailed(String.format("Failed to stop all nodes, %s", stopNodesCommand.getAllErrors()));
                }
            }
        });

        return po.getId();
    }

    public UUID startNode(final String clusterName, final String lxcHostname) {
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

                po.addLog(String.format("Starting node %s...", node.getHostname()));

                Command startNodeCommand = Commands.getStartCommand(Util.wrapAgentToSet(node));
                final AtomicBoolean ok = new AtomicBoolean();
                commandRunner.runCommand(startNodeCommand, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains("Started")) {
                            ok.set(true);
                            stop();
                        }
                    }

                });

                if (ok.get()) {
                    po.addLogDone(String.format("Node %s started", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Starting node %s failed, %s", node.getHostname(), startNodeCommand.getAllErrors()));
                }

            }
        });

        return po.getId();

    }

    public UUID stopNode(final String clusterName, final String lxcHostname) {
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

                po.addLog(String.format("Stopping node %s...", node.getHostname()));

                Command stopNodeCommand = Commands.getStopCommand(Util.wrapAgentToSet(node));
                commandRunner.runCommand(stopNodeCommand);

                if (stopNodeCommand.hasSucceeded()) {
                    po.addLogDone(String.format("Node %s stopped", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Stopping %s failed, %s", node.getHostname(), stopNodeCommand.getAllErrors()));
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

                Command checkNodeCommand = Commands.getStatusCommand(Util.wrapAgentToSet(node));
                commandRunner.runCommand(checkNodeCommand);

                if (checkNodeCommand.hasCompleted()) {
                    po.addLogDone(String.format("%s", checkNodeCommand.getResults().get(node.getUuid()).getStdOut()));
                } else {
                    po.addLogFailed(String.format("Faied to check status, %s", checkNodeCommand.getAllErrors()));
                }
            }
        });

        return po.getId();
    }

}
