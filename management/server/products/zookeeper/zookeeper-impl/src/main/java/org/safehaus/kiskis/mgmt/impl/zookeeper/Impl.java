package org.safehaus.kiskis.mgmt.impl.zookeeper;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.zookeeper.Config;
import org.safehaus.kiskis.mgmt.api.zookeeper.Api;
import org.safehaus.kiskis.mgmt.api.taskrunner.*;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Impl implements Api {

    private TaskRunner taskRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
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

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, String.format("Installing %s", Config.PRODUCT_KEY));

        executor.execute(new Runnable() {

            public void run() {

                if (config == null || Util.isStringEmpty(config.getZkName()) || Util.isStringEmpty(config.getClusterName()) || config.getNumberOfNodes() <= 0) {
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
                        Task installTask = taskRunner.executeTaskNWait(Tasks.getInstallTask(config.getNodes()));

                        if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Installation succeeded\nUpdating settings...");

                            //update settings
                            Task updateSettingsTask = taskRunner.executeTaskNWait(Tasks.getUpdateSettingsTask(config.getNodes(), config.getZkName()));

                            if (updateSettingsTask.getTaskStatus() == TaskStatus.SUCCESS) {

                                po.addLog(String.format("Settings updated\nStarting %s...", Config.PRODUCT_KEY));
                                //start all nodes
                                Task startTask = taskRunner.executeTaskNWait(Tasks.getStartTask(config.getNodes()));

                                if (startTask.getTaskStatus() == TaskStatus.SUCCESS) {
                                    po.addLogDone(String.format("Starting %s succeeded\nDone", Config.PRODUCT_KEY));
                                } else {
                                    po.addLogFailed(String.format("Starting %s failed, %s", Config.PRODUCT_KEY, startTask.getFirstError()));
                                }
                            } else {
                                po.addLogFailed(String.format("Failed to update settings, %s\nPlease update settings manually and restart the cluster, %s", updateSettingsTask.getFirstError()));
                            }
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
                        }

                    } else {
                        //destroy all lxcs also
                        Set<String> lxcHostnames = new HashSet<String>();
                        for (Agent lxcAgent : config.getNodes()) {
                            lxcHostnames.add(lxcAgent.getHostname());
                        }
                        try {
                            lxcManager.destroyLxcs(lxcHostnames);
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

                Set<String> lxcHostnames = new HashSet<String>();
                for (Agent lxcAgent : config.getNodes()) {
                    lxcHostnames.add(lxcAgent.getHostname());
                }
                try {
                    lxcManager.destroyLxcs(lxcHostnames);
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
                Task startNodeTask = Tasks.getStartTask(Util.wrapAgentToSet(node));
                final Task checkNodeTask = Tasks.getStatusTask(node);

                taskRunner.executeTaskNWait(startNodeTask, new TaskCallback() {

                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                        if (task.getData() == TaskType.START && task.isCompleted()) {
                            //run status check task
                            return checkNodeTask;

                        } else if (task.getData() == TaskType.STATUS) {
                            if (Util.isFinalResponse(response)) {
                                if (stdOut.contains("is running")) {
                                    task.setData(NodeState.RUNNING);
                                } else if (stdOut.contains("is not running")) {
                                    task.setData(NodeState.STOPPED);
                                }

                            }

                        }

                        return null;
                    }
                });

                if (NodeState.RUNNING.equals(checkNodeTask.getData())) {
                    po.addLogDone(String.format("Node on %s started", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to start node %s. %s",
                            lxcHostName,
                            startNodeTask.getFirstError()
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
                Task stopNodeTask = Tasks.getStopTask(Util.wrapAgentToSet(node));
                final Task checkNodeTask = Tasks.getStatusTask(node);

                taskRunner.executeTaskNWait(stopNodeTask, new TaskCallback() {

                    @Override
                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                        if (task.getData() == TaskType.STOP && task.isCompleted()) {
                            //run status check task
                            return checkNodeTask;

                        } else if (task.getData() == TaskType.STATUS) {
                            if (Util.isFinalResponse(response)) {
                                if (stdOut.contains("is running")) {
                                    task.setData(NodeState.RUNNING);
                                } else if (stdOut.contains("is not running")) {
                                    task.setData(NodeState.STOPPED);
                                }

                            }

                        }

                        return null;
                    }
                });

                if (NodeState.STOPPED.equals(checkNodeTask.getData())) {
                    po.addLogDone(String.format("Node on %s stopped", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            lxcHostName,
                            stopNodeTask.getFirstError()
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
                final Task checkNodeTask = taskRunner.executeTaskNWait(Tasks.getStatusTask(node));

                NodeState nodeState = NodeState.UNKNOWN;
                if (checkNodeTask.isCompleted()) {
                    Result result = checkNodeTask.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut().contains("is running")) {
                        nodeState = NodeState.RUNNING;
                    } else if (result.getStdOut().contains("is not running")) {
                        nodeState = NodeState.STOPPED;
                    }
                }

                if (NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            lxcHostName,
                            checkNodeTask.getFirstError()
                    ));
                } else {
                    po.addLogDone(String.format("Node %s is %s",
                            lxcHostName,
                            nodeState
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
                Task updateSettingsTask = taskRunner.executeTaskNWait(Tasks.getUpdateSettingsTask(config.getNodes(), config.getZkName()));

                if (updateSettingsTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLog("Settings updated\nRestarting cluster...");
                    //restart all other nodes with new configuration
                    Task restartTask = taskRunner.executeTaskNWait(Tasks.getRestartTask(config.getNodes()));

                    if (restartTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Cluster successfully restarted");
                    } else {
                        po.addLog(String.format("Failed to restart cluster, %s, skipping...", restartTask.getFirstError()));
                    }
                } else {
                    po.addLog(
                            String.format(
                                    "Settings update failed, %s\nPlease update settings manually and restart the cluster, skipping...",
                                    updateSettingsTask.getFirstError()));
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
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
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
                    Task installTask = taskRunner.executeTaskNWait(Tasks.getInstallTask(Util.wrapAgentToSet(lxcAgent)));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Installation succeeded\nUpdating db...");
                        //update db
                        if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                            po.addLog("Cluster info updated in DB\nUpdating settings...");

                            //update settings
                            Task updateSettingsTask = taskRunner.executeTaskNWait(Tasks.getUpdateSettingsTask(config.getNodes(), config.getZkName()));

                            if (updateSettingsTask.getTaskStatus() == TaskStatus.SUCCESS) {
                                po.addLog("Settings updated\nRestarting cluster...");
                                //restart all nodes
                                Task restartTask = taskRunner.executeTaskNWait(Tasks.getRestartTask(config.getNodes()));
                                if (restartTask.getTaskStatus() == TaskStatus.SUCCESS) {
                                    po.addLogDone("Cluster restarted successfully\nDone");
                                } else {
                                    po.addLogFailed("Failed to restart cluster");
                                }
                            } else {
                                po.addLogFailed(
                                        String.format(
                                                "Settings update failed, %s.\nPlease update settings manually and restart the cluster",
                                                installTask.getFirstError()));
                            }
                        } else {
                            po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                        }
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s\nUse LXC Module to cleanup",
                                installTask.getFirstError()));
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
