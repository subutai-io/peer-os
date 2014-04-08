/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.presto;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.presto.Config;
import org.safehaus.kiskis.mgmt.api.presto.Presto;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class PrestoImpl implements Presto {

    private TaskRunner taskRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Installing cluster %s", config.getClusterName()));

        executor.execute(new Runnable() {

            public void run() {
                if (config == null || Util.isStringEmpty(config.getClusterName()) || Util.isCollectionEmpty(config.getWorkers()) || config.getCoordinatorNode() == null) {
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
                for (Iterator<Agent> it = config.getWorkers().iterator(); it.hasNext();) {
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
                Task checkInstalled = taskRunner.executeTask(Tasks.getCheckInstalledTask(allNodes));

                if (!checkInstalled.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }
                for (Iterator<Agent> it = allNodes.iterator(); it.hasNext();) {
                    Agent node = it.next();
                    Result result = checkInstalled.getResults().get(node.getUuid());
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

                    Task installTask = taskRunner.executeTask(Tasks.getInstallTask(config.getAllNodes()));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Installation succeeded\nConfiguring coordinator...");

                        Task configureCoordinatorTask = taskRunner.executeTask(Tasks.getSetCoordinatorTask(config.getCoordinatorNode()));

                        if (configureCoordinatorTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Coordinator configured successfully\nConfiguring workers...");

                            Task configureWorkersTask = taskRunner.executeTask(Tasks.getSetWorkerTask(config.getCoordinatorNode(), config.getWorkers()));

                            if (configureWorkersTask.getTaskStatus() == TaskStatus.SUCCESS) {
                                po.addLog("Workers configured successfully\nStarting Presto...");

                                Task startPrestoTask = Tasks.getStartTask(config.getAllNodes());
                                final AtomicInteger okCount = new AtomicInteger(0);
                                taskRunner.executeTask(startPrestoTask, new TaskCallback() {

                                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                        okCount.set(Util.countNumberOfOccurences(stdOut, "Started"));

                                        if (okCount.get() == config.getAllNodes().size()) {
                                            taskRunner.removeTaskCallback(task.getUuid());
                                            synchronized (task) {
                                                task.notifyAll();
                                            }
                                        } else if (task.isCompleted()) {
                                            synchronized (task) {
                                                task.notifyAll();
                                            }
                                        }

                                        return null;
                                    }
                                });

                                synchronized (startPrestoTask) {
                                    try {
                                        startPrestoTask.wait(startPrestoTask.getAvgTimeout() * 1000 + 1000);
                                    } catch (InterruptedException ex) {
                                    }
                                }
                                if (okCount.get() == config.getAllNodes().size()) {
                                    po.addLogDone("Presto started successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to start Presto, %s", startPrestoTask.getFirstError()));
                                }

                            } else {
                                po.addLogFailed(String.format("Failed to configure workers, %s", configureWorkersTask.getFirstError()));
                            }
                        } else {
                            po.addLogFailed(String.format("Failed to configure coordinator, %s", configureCoordinatorTask.getFirstError()));
                        }

                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
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

                po.addLog("Uninstalling Presto...");

                Task uninstallTask = taskRunner.executeTask(Tasks.getUninstallTask(config.getAllNodes()));

                if (uninstallTask.isCompleted()) {
                    for (Map.Entry<UUID, Result> res : uninstallTask.getResults().entrySet()) {
                        Result result = res.getValue();
                        Agent agent = agentManager.getAgentByUUID(res.getKey());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-presto is not installed, so not removed")) {
                                po.addLog(String.format("Presto is not installed, so not removed on node %s", result.getStdErr(),
                                        agent == null ? res.getKey() : agent.getHostname()));
                            } else {
                                po.addLog(String.format("Presto is removed from node %s",
                                        agent == null ? res.getKey() : agent.getHostname()));
                            }
                        } else {
                            po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                                    agent == null ? res.getKey() : agent.getHostname()));
                        }
                    }
                    po.addLog("Updating db...");
                    if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                        po.addLogDone("Cluster info deleted from DB\nDone");
                    } else {
                        po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed(String.format("Uninstallation failed, %s", uninstallTask.getFirstError()));
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
                Task checkInstalled = taskRunner.executeTask(Tasks.getCheckInstalledTask(Util.wrapAgentToSet(agent)));

                if (!checkInstalled.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
                    return;
                }

                Result result = checkInstalled.getResults().get(agent.getUuid());

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
                    Task installTask = taskRunner.executeTask(Tasks.getInstallTask(Util.wrapAgentToSet(agent)));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Installation succeeded");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
                        return;
                    }

                    po.addLog("Configuring worker...");
                    Task configureWorkerTask = taskRunner.executeTask(Tasks.getSetWorkerTask(config.getCoordinatorNode(), Util.wrapAgentToSet(agent)));

                    if (configureWorkerTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Worker configured successfully\nStarting Presto on new node...");

                        Task startPrestoTask = Tasks.getStartTask(Util.wrapAgentToSet(agent));
                        final AtomicInteger okCount = new AtomicInteger(0);
                        taskRunner.executeTask(startPrestoTask, new TaskCallback() {

                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                if (stdOut.contains("Started")) {
                                    okCount.incrementAndGet();
                                }

                                if (okCount.get() > 0) {
                                    taskRunner.removeTaskCallback(task.getUuid());
                                    synchronized (task) {
                                        task.notifyAll();
                                    }
                                } else if (task.isCompleted()) {
                                    synchronized (task) {
                                        task.notifyAll();
                                    }
                                }

                                return null;
                            }
                        });

                        synchronized (startPrestoTask) {
                            try {
                                startPrestoTask.wait(startPrestoTask.getAvgTimeout() * 1000 + 1000);
                            } catch (InterruptedException ex) {
                            }
                        }

                        if (okCount.get() > 0) {
                            po.addLogDone("Presto started successfully\nDone");
                        } else {
                            po.addLogFailed(String.format("Failed to start Presto, %s", startPrestoTask.getFirstError()));
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to configure worker, %s", configureWorkerTask.getFirstError()));
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

                Task uninstallTask = taskRunner.executeTask(Tasks.getUninstallTask(Util.wrapAgentToSet(agent)));

                if (uninstallTask.isCompleted()) {
                    Map.Entry<UUID, Result> res = uninstallTask.getResults().entrySet().iterator().next();
                    Result result = res.getValue();
                    if (result.getExitCode() != null && result.getExitCode() == 0) {
                        if (result.getStdOut().contains("Package ksks-presto is not installed, so not removed")) {
                            po.addLog(String.format("Presto is not installed, so not removed on node %s", result.getStdErr(),
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
                    po.addLogFailed(String.format("Uninstallation failed, %s", uninstallTask.getFirstError()));
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
                Task stopAllNodesTask = taskRunner.executeTask(Tasks.getStopTask(config.getAllNodes()));
                if (stopAllNodesTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLog("All nodes stopped\nConfiguring coordinator...");
                    Task configureCoordinatorTask = taskRunner.executeTask(Tasks.getSetCoordinatorTask(newCoordinator));
                    if (configureCoordinatorTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Coordinator configured successfully");
                    } else {
                        po.addLogFailed(String.format("Failed to configure coordinator, %s\nOperation aborted", configureCoordinatorTask.getFirstError()));
                        return;
                    }

                    config.getWorkers().add(config.getCoordinatorNode());
                    config.getWorkers().remove(newCoordinator);
                    config.setCoordinatorNode(newCoordinator);

                    po.addLog("Configuring workers...");
                    Task configureWorkersTask = taskRunner.executeTask(Tasks.getSetWorkerTask(newCoordinator, config.getWorkers()));
                    if (configureWorkersTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Workers configured successfully\nStarting cluster...");

                        Task startPrestoTask = Tasks.getStartTask(config.getAllNodes());
                        final AtomicInteger okCount = new AtomicInteger(0);
                        taskRunner.executeTask(startPrestoTask, new TaskCallback() {

                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                okCount.set(Util.countNumberOfOccurences(stdOut, "Started"));

                                if (okCount.get() == config.getAllNodes().size()) {
                                    taskRunner.removeTaskCallback(task.getUuid());
                                    synchronized (task) {
                                        task.notifyAll();
                                    }
                                } else if (task.isCompleted()) {
                                    synchronized (task) {
                                        task.notifyAll();
                                    }
                                }

                                return null;
                            }
                        });

                        synchronized (startPrestoTask) {
                            try {
                                startPrestoTask.wait(startPrestoTask.getAvgTimeout() * 1000 + 1000);
                            } catch (InterruptedException ex) {
                            }
                        }
                        if (okCount.get() == config.getAllNodes().size()) {
                            po.addLog("Cluster started successfully");
                        } else {
                            po.addLog(String.format("Start of cluster failed, %s, skipping...", startPrestoTask.getFirstError()));
                        }

                        po.addLog("Updating db...");
                        //update db
                        if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                            po.addLogDone("Cluster info updated in DB\nDone");
                        } else {
                            po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to configure workers, %s\nOperation aborted", configureWorkersTask.getFirstError()));
                    }

                } else {
                    po.addLogFailed(String.format("Failed to stop all nodes, %s", stopAllNodesTask.getFirstError()));
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

                Task startTask = Tasks.getStartTask(Util.wrapAgentToSet(node));

                final AtomicInteger okCount = new AtomicInteger(0);
                taskRunner.executeTask(startTask, new TaskCallback() {

                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        if (stdOut.contains("Started")) {
                            okCount.incrementAndGet();
                        }

                        if (okCount.get() > 0) {
                            taskRunner.removeTaskCallback(task.getUuid());
                            synchronized (task) {
                                task.notifyAll();
                            }
                        } else if (task.isCompleted()) {
                            synchronized (task) {
                                task.notifyAll();
                            }
                        }

                        return null;
                    }
                });

                synchronized (startTask) {
                    try {
                        startTask.wait(startTask.getAvgTimeout() * 1000 + 1000);
                    } catch (InterruptedException ex) {
                    }
                }
                if (okCount.get() > 0) {
                    po.addLogDone(String.format("Node %s started", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Starting node %s failed, %s", node.getHostname(), startTask.getFirstError()));
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

                Task stopTask = taskRunner.executeTask(Tasks.getStopTask(Util.wrapAgentToSet(node)));

                if (stopTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone(String.format("Node %s stopped", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Stopping %s failed, %s", node.getHostname(), stopTask.getFirstError()));
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

                Task checkNodeTask = taskRunner.executeTask(
                        Tasks.getStatusTask(Util.wrapAgentToSet(node)));

                Result res = checkNodeTask.getResults().get(node.getUuid());
                if (checkNodeTask.isCompleted()) {
                    po.addLogDone(String.format("%s", res.getStdOut()));
                } else {
                    po.addLogFailed(String.format("Faied to check status, %s", res.getStdErr()));
                }
            }
        });

        return po.getId();
    }

}
