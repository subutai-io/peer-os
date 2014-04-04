/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

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
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.api.spark.Spark;
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
 * @todo check destroy slave on master & change master actions
 */
public class SparkImpl implements Spark {

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
                if (config == null || Util.isCollectionEmpty(config.getSlaveNodes()) || config.getMasterNode() == null) {
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
                for (Iterator<Agent> it = config.getSlaveNodes().iterator(); it.hasNext();) {
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
                Task checkInstalled = taskRunner.executeTask(Tasks.getCheckInstalledTask(allNodes));

                if (!checkInstalled.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }
                for (Iterator<Agent> it = allNodes.iterator(); it.hasNext();) {
                    Agent node = it.next();
                    Result result = checkInstalled.getResults().get(node.getUuid());
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

                    Task installTask = taskRunner.executeTask(Tasks.getInstallTask(config.getAllNodes()));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Installation succeeded\nSetting master IP...");

                        Task setMasterIPTask = taskRunner.executeTask(Tasks.getSetMasterIPTask(config.getAllNodes(), config.getMasterNode()));

                        if (setMasterIPTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Setting master IP succeeded\nRegistering slaves...");

                            Task registerSlavesTask = taskRunner.executeTask(Tasks.getAddSlavesTask(config.getMasterNode(), config.getSlaveNodes()));

                            if (registerSlavesTask.getTaskStatus() == TaskStatus.SUCCESS) {
                                po.addLog("Slaves successfully registered\nStarting Spark...");

                                Task startSparkTask = Tasks.getStartAllTask(Util.wrapAgentToSet(config.getMasterNode()));
                                final AtomicInteger okCount = new AtomicInteger(0);
                                taskRunner.executeTask(startSparkTask, new TaskCallback() {

                                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                        okCount.set(Util.countNumberOfOccurences(stdOut, "starting"));

                                        if (okCount.get() >= config.getAllNodes().size()) {
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

                                synchronized (startSparkTask) {
                                    try {
                                        startSparkTask.wait(startSparkTask.getAvgTimeout() * 1000 + 1000);
                                    } catch (InterruptedException ex) {
                                    }
                                }
                                if (okCount.get() >= config.getAllNodes().size()) {
                                    po.addLogDone("Spark started successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to start Spark, %s", startSparkTask.getFirstError()));
                                }

                            } else {
                                po.addLogFailed(String.format("Failed to register slaves with master, %s", registerSlavesTask.getFirstError()));
                            }
                        } else {
                            po.addLogFailed(String.format("Setting master IP failed, %s", setMasterIPTask.getFirstError()));
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

                po.addLog("Uninstalling Spark...");

                Task uninstallTask = taskRunner.executeTask(Tasks.getUninstallTask(config.getAllNodes()));

                if (uninstallTask.isCompleted()) {
                    for (Map.Entry<UUID, Result> res : uninstallTask.getResults().entrySet()) {
                        Result result = res.getValue();
                        Agent agent = agentManager.getAgentByUUID(res.getKey());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-spark is not installed, so not removed")) {
                                po.addLog(String.format("Spark is not installed, so not removed on node %s", result.getStdErr(),
                                        agent == null ? res.getKey() : agent.getHostname()));
                            } else {
                                po.addLog(String.format("Spark is removed from node %s",
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
                Task checkInstalled = taskRunner.executeTask(Tasks.getCheckInstalledTask(Util.wrapAgentToSet(agent)));

                if (!checkInstalled.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
                    return;
                }

                Result result = checkInstalled.getResults().get(agent.getUuid());

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
                        Task installTask = taskRunner.executeTask(Tasks.getInstallTask(Util.wrapAgentToSet(agent)));

                        if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Installation succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
                            return;
                        }
                    }

                    po.addLog("Setting master IP on slave...");
                    Task setMasterIPTask = taskRunner.executeTask(Tasks.getSetMasterIPTask(Util.wrapAgentToSet(agent), config.getMasterNode()));

                    if (setMasterIPTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Master IP successfully set\nRegistering slave with master...");

                        Task registerSlaveTask = taskRunner.executeTask(Tasks.getAddSlaveTask(config.getMasterNode(), agent));

                        if (registerSlaveTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Registration succeeded\nRestarting master...");

                            Task restartMasterTask = taskRunner.executeTask(Tasks.getRestartMasterTask(config.getMasterNode()));

                            if (restartMasterTask.getTaskStatus() == TaskStatus.SUCCESS) {
                                po.addLog("Master restarted successfully\nStarting Spark on new node...");

                                Task startSparkTask = Tasks.getStartSlaveTask(Util.wrapAgentToSet(agent));
                                final AtomicInteger okCount = new AtomicInteger(0);
                                taskRunner.executeTask(startSparkTask, new TaskCallback() {

                                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                        if (stdOut.contains("starting")) {
                                            okCount.incrementAndGet();
                                        }

                                        if (okCount.get() >= 0) {
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

                                synchronized (startSparkTask) {
                                    try {
                                        startSparkTask.wait(startSparkTask.getAvgTimeout() * 1000 + 1000);
                                    } catch (InterruptedException ex) {
                                    }
                                }

                                if (okCount.get() >= 0) {
                                    po.addLogDone("Spark started successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Failed to start Spark, %s", startSparkTask.getFirstError()));
                                }

                            } else {
                                po.addLogFailed(String.format("Master restart failed, %s", restartMasterTask.getFirstError()));
                            }

                        } else {
                            po.addLogFailed(String.format("Registration failed, %s", registerSlaveTask.getFirstError()));
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to set master IP, %s", setMasterIPTask.getFirstError()));
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

                    Task unregisterSlaveTask = taskRunner.executeTask(Tasks.getRemoveSlaveTask(config.getMasterNode(), agent));

                    if (unregisterSlaveTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Successfully unregistered slave from master\nRestarting master...");

                        Task restartMasterTask = taskRunner.executeTask(Tasks.getRestartMasterTask(config.getMasterNode()));

                        if (restartMasterTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Master restarted successfully");
                        } else {
                            po.addLog(String.format("Master restart failed, %s, skipping...", restartMasterTask.getFirstError()));
                        }
                    } else {
                        po.addLog(String.format("Failed to unregister slave from master: %s, skipping...",
                                unregisterSlaveTask.getFirstError()));
                    }
                } else {
                    po.addLog("Failed to unregister slave from master: Master is not connected, skipping...");
                }

                boolean uninstall = !agent.equals(config.getMasterNode());

                if (uninstall) {
                    po.addLog("Uninstalling Spark...");

                    Task uninstallTask = taskRunner.executeTask(Tasks.getUninstallTask(Util.wrapAgentToSet(agent)));

                    if (uninstallTask.isCompleted()) {
                        Map.Entry<UUID, Result> res = uninstallTask.getResults().entrySet().iterator().next();
                        Result result = res.getValue();
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("Package ksks-spark is not installed, so not removed")) {
                                po.addLog(String.format("Spark is not installed, so not removed on node %s", result.getStdErr(),
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
                        po.addLogFailed(String.format("Uninstallation failed, %s", uninstallTask.getFirstError()));
                        return;
                    }
                } else {
                    po.addLog("Stopping slave...");

                    Task stopSlaveTask = taskRunner.executeTask(Tasks.getStopSlaveTask(Util.wrapAgentToSet(agent)));

                    if (stopSlaveTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Slave stopped successfully");
                    } else {
                        po.addLog(String.format("Failed to stop slave, %s, skipping...", stopSlaveTask.getFirstError()));
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
                Task stopAllNodesTask = taskRunner.executeTask(Tasks.getStopAllTask(Util.wrapAgentToSet(config.getMasterNode())));
                if (stopAllNodesTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLog("All nodes stopped\nClearing slaves on old master...");
                    //clear slaves from old master
                    Task clearSlavesTask = taskRunner.executeTask(Tasks.getClearSlavesTask(config.getMasterNode()));
                    if (clearSlavesTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Slaves cleared successfully");
                    } else {
                        po.addLog(String.format("Clearing slaves failed, %s, skipping...", clearSlavesTask.getFirstError()));
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
                    Task addSlavesTask = taskRunner.executeTask(Tasks.getAddSlavesTask(newMaster, config.getSlaveNodes()));
                    if (addSlavesTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Nodes added successfully\nSetting new master IP...");
                        //modify master ip on all nodes
                        Task setMasterIPTask = taskRunner.executeTask(Tasks.getSetMasterIPTask(config.getAllNodes(), newMaster));
                        if (setMasterIPTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLog("Master IP set successfully\nStarting cluster...");
                            //start master & slaves

                            Task startSparkTask = Tasks.getStartAllTask(Util.wrapAgentToSet(config.getMasterNode()));
                            final AtomicInteger okCount = new AtomicInteger(0);
                            taskRunner.executeTask(startSparkTask, new TaskCallback() {

                                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                    okCount.set(Util.countNumberOfOccurences(stdOut, "starting"));

                                    if (okCount.get() >= config.getAllNodes().size()) {
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

                            synchronized (startSparkTask) {
                                try {
                                    startSparkTask.wait(startSparkTask.getAvgTimeout() * 1000 + 1000);
                                } catch (InterruptedException ex) {
                                }
                            }
                            if (okCount.get() >= config.getAllNodes().size()) {
                                po.addLog("Cluster started successfully");
                            } else {
                                po.addLog(String.format("Start of cluster failed, %s, skipping...", startSparkTask.getFirstError()));
                            }

                            po.addLog("Updating db...");
                            //update db
                            if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                                po.addLogDone("Cluster info updated in DB\nDone");
                            } else {
                                po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                            }
                        } else {
                            po.addLogFailed(String.format("Failed to set master IP on all nodes, %s\nOperation aborted", setMasterIPTask.getFirstError()));
                        }
                    } else {
                        po.addLogFailed(String.format("Failed to add slaves to new master, %s\nOperation aborted", addSlavesTask.getFirstError()));
                    }

                } else {
                    po.addLogFailed(String.format("Failed to stop all nodes, %s", stopAllNodesTask.getFirstError()));
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

                Task startTask;
                if (master) {
                    startTask = Tasks.getStartMasterTask(Util.wrapAgentToSet(node));
                } else {
                    startTask = Tasks.getStartSlaveTask(Util.wrapAgentToSet(node));
                }

                final AtomicInteger okCount = new AtomicInteger(0);
                taskRunner.executeTask(startTask, new TaskCallback() {

                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        okCount.set(Util.countNumberOfOccurences(stdOut, "starting"));

                        if (okCount.get() >= 0) {
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
                if (okCount.get() >= 0) {
                    po.addLogDone(String.format("Node %s started", node.getHostname()));
                } else {
                    po.addLogFailed(String.format("Starting node %s failed, %s", node.getHostname(), startTask.getFirstError()));
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

                Task stopTask;
                if (master) {
                    stopTask = taskRunner.executeTask(Tasks.getStopMasterTask(Util.wrapAgentToSet(node)));
                } else {
                    stopTask = taskRunner.executeTask(Tasks.getStopSlaveTask(Util.wrapAgentToSet(node)));
                }

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
                        Tasks.getStatusAllTask(Util.wrapAgentToSet(node)));

                Result res = checkNodeTask.getResults().get(node.getUuid());
                if (checkNodeTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone(String.format("%s", res.getStdOut()));
                } else {
                    po.addLogFailed(String.format("Faied to check status, %s", res.getStdErr()));
                }
            }
        });

        return po.getId();
    }

}
