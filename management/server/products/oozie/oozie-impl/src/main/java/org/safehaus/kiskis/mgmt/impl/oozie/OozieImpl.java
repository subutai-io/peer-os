package org.safehaus.kiskis.mgmt.impl.oozie;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.oozie.Oozie;
import org.safehaus.kiskis.mgmt.api.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OozieImpl implements Oozie {

    public static final String MODULE_NAME = "Oozie";
    private TaskRunner taskRunner;
    public AgentManager agentManager;
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

    public UUID installCluster(final OozieConfig config) {
        final ProductOperation po = tracker.createProductOperation(OozieConfig.PRODUCT_KEY, "Installing Oozie");

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(config.PRODUCT_KEY, config.getClusterName(), OozieConfig.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (dbManager.saveInfo(config.PRODUCT_KEY, config.getClusterName(), config)) {


                    // Installing Oozie server
                    po.addLog("Cluster info saved to DB\nInstalling Oozie server...");
                    Task installServerTask = taskRunner.executeTaskNWait(Tasks.getInstallServerTask(config.getServer()));

                    if (installServerTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Installation failed, %s", installServerTask.getFirstError()));
                        return;
                    }

                    // Installing Oozie client
                    po.addLog("Cluster info saved to DB\nInstalling Oozie clients...");
                    Task installClientsTask = taskRunner.executeTaskNWait(Tasks.getInstallClientsTask(config.getClients()));

                    if (installClientsTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Installation failed, %s", installClientsTask.getFirstError()));
                        return;
                    }


                    // Configuring root hosts
                    po.addLog("Installation succeeded\nConfiguring root hosts...");
                    Task configMasterTask = taskRunner.executeTaskNWait(Tasks.getConfigureRootHostsTask(config.getHadoopNodes(),  config.getServer().getListIP().get(0)));

                    if (configMasterTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Configuration failed, %s", configMasterTask.getFirstError()));
                        return;
                    }
                    po.addLog("Configuring root hosts succeeded\nConfiguring root groups...");

                    // Configuring root groups
                    Task configRegionTask = taskRunner.executeTaskNWait(Tasks.getConfigureRootGroupsTask(config.getHadoopNodes()));

                    if (configRegionTask.getTaskStatus() != TaskStatus.SUCCESS) {
                        po.addLogFailed(String.format("Configuring failed, %s", configRegionTask.getFirstError()));
                        return;
                    }
                    po.addLog("Configuring root groups succeeded\n");

                    po.addLog("Oozie installation succeeded\n");


                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(OozieConfig.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                OozieConfig config = dbManager.getInfo(OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Task uninstallServerTask = taskRunner.executeTaskNWait(Tasks.getUninstallServerTask(config.getServer()));

                if (uninstallServerTask.getTaskStatus() != TaskStatus.SUCCESS) {
                    po.addLogFailed(String.format("Uninstallation server failed, %s", uninstallServerTask.getFirstError()));
                    return;
                }
                
                Task uninstallClientsTask = taskRunner.executeTaskNWait(Tasks.getUninstallClientsTask(config.getClients()));

                if (uninstallClientsTask.getTaskStatus() != TaskStatus.SUCCESS) {
                    po.addLogFailed(String.format("Uninstallation clients failed, %s", uninstallClientsTask.getFirstError()));
                    return;
                }


                po.addLog("Updating db...");
                if (dbManager.deleteInfo(OozieConfig.PRODUCT_KEY, config.getClusterName())) {
                    po.addLogDone("Cluster info deleted from DB\nDone");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                }

            }
        });

        return po.getId();
    }

    /*public UUID startNode(final String clusterName, final String lxcHostName) {
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
    }*/

    /*public UUID stopNode(final String clusterName, final String lxcHostName) {
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
    }*/

    /*public UUID checkNode(final String clusterName, final String lxcHostName) {
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
                po.addLog("Checking node...");
                final Task checkNodeTask = taskRunner.executeTask(Tasks.getStatusTask(node));

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
    }*/

    public UUID destroyNode(final String clusterName, final String lxcHostName) {
        return null;
    }

    /*public UUID destroyNode(final String clusterName, final String lxcHostName) {
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
                                    agent.getHostname()));
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
                if (!dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
                            config.getClusterName()));
                } else {
                    po.addLogDone("Done");
                }
            }
        });

        return po.getId();
    }*/

    /*public UUID addNode(final String clusterName) {
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

                    po.addLog("Creating lxc container...");

                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(1);

                    Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

                    config.getNodes().add(lxcAgent);
                    po.addLog("Lxc container created successfully\nUpdating db...");
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
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
    }*/

    public List<OozieConfig> getClusters() {

        return dbManager.getInfo(OozieConfig.PRODUCT_KEY, OozieConfig.class);

    }

    @Override
    public UUID startServer(Agent agent) {
        return null;
    }

    @Override
    public UUID stopServer(Agent agent) {
        return null;
    }

    @Override
    public UUID checkServerStatus(Agent agent) {
        return null;
    }


}
