package org.safehaus.kiskis.mgmt.impl.hbase;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseType;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
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

public class HBaseImpl implements HBase {

    public static final String MODULE_NAME = "HBase";
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

    public UUID installCluster(final HBaseConfig config) {
        final ProductOperation po = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY, "Installing HBase");

        final Set<Agent> allNodes = new HashSet<Agent>();
        allNodes.add(config.getMaster());
        allNodes.addAll(config.getRegion());
        allNodes.addAll(config.getQuorum());
        allNodes.add(config.getBackupMasters());

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(HBaseConfig.PRODUCT_KEY, config.getClusterName(), HBaseConfig.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (dbManager.saveInfo(HBaseConfig.PRODUCT_KEY, config.getClusterName(), config)) {

                    po.addLog("Cluster info saved to DB\nInstalling HBase...");

                    // Updating repository
                    Task repositoryUpdateTask = taskRunner.executeTask(Tasks.getAptUpdate(allNodes));

                    if (repositoryUpdateTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Installation failed, %s", repositoryUpdateTask.getFirstError()));
                        return;
                    }
                    po.addLog("Installation succeeded\nSetting master...");

                    // Installing HBase
                    Task installTask = taskRunner.executeTask(Tasks.getInstallTask(allNodes));

                    if (installTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
                        return;
                    }
                    po.addLog("Installation succeeded\nConfiguring master...");

                    // Configuring master
                    Task configMasterTask = taskRunner.executeTask(Tasks.getConfigMasterTask(allNodes, config.getHadoopNameNode(), config.getMaster()));

                    if (configMasterTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Configuration failed, %s", configMasterTask.getFirstError()));
                        return;
                    }
                    po.addLog("Configuring master succeeded\nConfiguring region...");

                    // Configuring region
                    Task configRegionTask = taskRunner.executeTask(Tasks.getConfigRegionTask(allNodes, config.getRegion()));

                    if (configRegionTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Configuring failed, %s", configRegionTask.getFirstError()));
                        return;
                    }
                    po.addLog("Configuring region succeeded\nSetting quorum...");

                    // Configuring quorum
                    Task configQuorumTask = taskRunner.executeTask(Tasks.getConfigQuorumTask(allNodes, config.getQuorum()));

                    if (configQuorumTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Installation failed, %s", configQuorumTask.getFirstError()));
                        return;
                    }
                    po.addLog("Setting quorum succeeded\nSetting backup masters...");

                    // Configuring backup master
                    Task configBackupMastersTask = taskRunner.executeTask(Tasks.getConfigBackupMastersTask(allNodes, config.getBackupMasters()));

                    if (configBackupMastersTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Installation failed, %s", configBackupMastersTask.getFirstError()));
                        return;
                    }
                    po.addLog("Setting backup masters succeeded\n");
                    po.addLog("Cluster installation succeeded\n");


                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(HBaseConfig config) {
        final String clusterName = config.getClusterName();
        final ProductOperation po
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));
        final Set<Agent> allNodes = new HashSet<Agent>();
        allNodes.add(config.getMaster());
        allNodes.addAll(config.getRegion());
        allNodes.addAll(config.getQuorum());
        allNodes.add(config.getBackupMasters());

        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Task installTask = taskRunner.executeTask(Tasks.getInstallTask(allNodes));

                if (installTask.getTaskStatus() != TaskStatus.SUCCESS) {
                    po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
                    return;
                }


                po.addLog("Updating db...");
                if (dbManager.deleteInfo(HBaseConfig.PRODUCT_KEY, config.getClusterName())) {
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
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }
                po.addLog("Starting node...");
                Task startNodeTask = Tasks.getStartTask(node);
                final Task checkNodeTask = Tasks.getStatusTask(node);

                taskRunner.executeTask(startNodeTask, new TaskCallback() {

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

                                synchronized (task) {
                                    task.notifyAll();
                                }
                            }

                        }

                        return null;
                    }
                });

                synchronized (checkNodeTask) {
                    try {
                        checkNodeTask.wait((checkNodeTask.getAvgTimeout() + startNodeTask.getAvgTimeout()) * 1000 + 1000);
                    } catch (InterruptedException ex) {
                    }
                }

                if (NodeState.RUNNING.equals(checkNodeTask.getData())) {
                    po.addLogDone(String.format("Node on %s started", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to start node %s. %s",
                            lxcHostName,
                            startNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID stopNode(final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }
                po.addLog("Stopping node...");
                Task stopNodeTask = Tasks.getStopTask(node);
                final Task checkNodeTask = Tasks.getStatusTask(node);

                taskRunner.executeTask(stopNodeTask, new TaskCallback() {

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

                                synchronized (task) {
                                    task.notifyAll();
                                }
                            }

                        }

                        return null;
                    }
                });

                synchronized (checkNodeTask) {
                    try {
                        checkNodeTask.wait((checkNodeTask.getAvgTimeout() + stopNodeTask.getAvgTimeout()) * 1000 + 1000);
                    } catch (InterruptedException ex) {
                    }
                }

                if (NodeState.STOPPED.equals(checkNodeTask.getData())) {
                    po.addLogDone(String.format("Node on %s stopped", lxcHostName));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            lxcHostName,
                            stopNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final HBaseType type, final String clusterName, final String lxcHostName) {
        final ProductOperation po
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Checking node %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostName);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
                    return;
                }
                po.addLog("Checking node...");
                final Task checkNodeTask = taskRunner.executeTask(Tasks.getStatusTask(node));

                NodeState nodeState = NodeState.UNKNOWN;
                if (checkNodeTask.isCompleted()) {
                    Result result = checkNodeTask.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut().contains(type.getRunningMsg())) {
                        nodeState = NodeState.RUNNING;
                    } else if (result.getStdOut().contains(type.getNotRunningMsg())) {
                        nodeState = NodeState.STOPPED;
                    }
                }

                if (NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            lxcHostName,
                            checkNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
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
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostName, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                final HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostName);
                if (agent == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostName));
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
                //update db
                po.addLog("Updating db...");
                config.getNodes().remove(agent);
                if (!dbManager.saveInfo(HBaseConfig.PRODUCT_KEY, config.getClusterName(), config)) {
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
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                try {

                    po.addLog("Creating lxc container...");

                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(1);

                    Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

                    config.getNodes().add(lxcAgent);
                    po.addLog("Lxc container created successfully\nUpdating db...");
                    if (dbManager.saveInfo(HBaseConfig.PRODUCT_KEY, clusterName, config)) {
                        po.addLog("Cluster info updated in DB\nInstalling Solr...");

                        Task installTask = taskRunner.executeTask(Tasks.getInstallTask(Util.wrapAgentToSet(lxcAgent)));

                        if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                            po.addLogDone("Installation succeeded\nDone");

                        } else {
                            po.addLogFailed(String.format("Installation failed, %s",
                                    installTask.getResults().entrySet().iterator().next().getValue().getStdErr()));
                        }
                    } else {
                        po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                    }

                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }
            }
        });

        return po.getId();
    }

    public List<HBaseConfig> getClusters() {

        return dbManager.getInfo(HBaseConfig.PRODUCT_KEY, HBaseConfig.class);

    }

}
