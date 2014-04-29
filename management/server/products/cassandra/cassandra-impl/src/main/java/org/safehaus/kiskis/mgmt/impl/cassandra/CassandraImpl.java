package org.safehaus.kiskis.mgmt.impl.cassandra;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.cassandra.Cassandra;
import org.safehaus.kiskis.mgmt.api.cassandra.Config;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.networkmanager.NetworkManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import sun.print.resources.serviceui;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CassandraImpl implements Cassandra {

    public static final String MODULE_NAME = "Cassandra";
    private TaskRunner taskRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private ExecutorService executor;
    private NetworkManager networkManager;
    private CommandRunner commandRunner;

    public void init() {
        Commands.init(commandRunner);
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

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, "Installing Cassandra");

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d lxc containers for Cassandra cluster...", config.getNumberOfNodes()));
                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(config.getNumberOfNodes());
                    config.setNodes(new HashSet<Agent>());

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        config.getNodes().addAll(entry.getValue());
                    }

                    Set<Agent> seedNodes = new HashSet<Agent>();
                    for (Iterator<Agent> iterator = seedNodes.iterator(); iterator.hasNext(); ) {
                        Agent agent = iterator.next();
                        seedNodes.add(agent);
                        if (seedNodes.size() == config.getNumberOfSeeds()) break;
                    }
                    config.setSeedNodes(seedNodes);

                    po.addLog("Lxc containers created successfully\nUpdating db...");
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLog("Cluster info saved to DB");

                        po.addLog("Configuring networking between nodes...");
                        if (networkManager.configHostsOnAgents(new ArrayList<Agent>(config.getNumberOfNodes()), config.getDomainName()) &&
                                networkManager.configSshOnAgents(new ArrayList<Agent>(config.getNumberOfNodes()))) {
                            po.addLog("\nNetwork configuration done...");
                        } else {
                            po.addLogFailed(String.format("Network configuration failed..."));
                            return;
                        }

                        //install

                        po.addLog("Installing...");
                        Command installCommand = Commands.getInstallCommand(config.getNodes());
                        commandRunner.runCommand(installCommand);

                        if (installCommand.hasSucceeded()) {
                            po.addLog("Installation succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                            return;
                        }

                        // setting cluster name
                        po.addLog("Setting cluster name " + config.getClusterName());
                        Command setClusterNameCommand = Commands.getConfigureCommand(config.getNodes(), "cluster_name " + config.getClusterName());
                        commandRunner.runCommand(setClusterNameCommand);

                        if (setClusterNameCommand.hasSucceeded()) {
                            po.addLog("Configure cluster name succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setClusterNameCommand.getAllErrors()));
                            return;
                        }

                        // setting data directory name
                        po.addLog("Setting data directory: " + config.getDataDirectory());
                        Command setDataDirCommand = Commands.getConfigureCommand(config.getNodes(), "data_dir " + config.getDataDirectory());
                        commandRunner.runCommand(setDataDirCommand);

                        if (setDataDirCommand.hasSucceeded()) {
                            po.addLog("Configure data directory succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setDataDirCommand.getAllErrors()));
                            return;
                        }

                        // setting commit log directory
                        po.addLog("Setting commit directory: " + config.getCommitLogDirectory());
                        Command setCommitDirCommand = Commands.getConfigureCommand(config.getNodes(), "commitlog_dir " + config.getCommitLogDirectory());
                        commandRunner.runCommand(setCommitDirCommand);

                        if (setCommitDirCommand.hasSucceeded()) {
                            po.addLog("Configure commit directory succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setCommitDirCommand.getAllErrors()));
                            return;
                        }

                        // setting saved cache directory
                        po.addLog("Setting saved cache directory: " + config.getSavedCachesDirectory());
                        Command setSavedCacheDirCommand = Commands.getConfigureCommand(config.getNodes(), "saved_cache_dir " + config.getSavedCachesDirectory());
                        commandRunner.runCommand(setSavedCacheDirCommand);

                        if (setSavedCacheDirCommand.hasSucceeded()) {
                            po.addLog("Configure saved cache directory succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setSavedCacheDirCommand.getAllErrors()));
                            return;
                        }

                        // setting rpc address
                        po.addLog("Setting rpc address");
                        Command setRpcAddressCommand = Commands.getConfigureRpcAndListenAddressesCommand(config.getNodes(), "rpc_address");
                        commandRunner.runCommand(setRpcAddressCommand);

                        if (setRpcAddressCommand.hasSucceeded()) {
                            po.addLog("Configure rpc address succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setRpcAddressCommand.getAllErrors()));
                            return;
                        }

                        // setting listen address
                        po.addLog("Setting listen address");
                        Command setListenAddressCommand = Commands.getConfigureRpcAndListenAddressesCommand(config.getNodes(), "listen_address");
                        commandRunner.runCommand(setListenAddressCommand);

                        if (setListenAddressCommand.hasSucceeded()) {
                            po.addLog("Configure listen address succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setListenAddressCommand.getAllErrors()));
                            return;
                        }

                        // setting seeds
                        StringBuilder sb = new StringBuilder();
                        sb.append('"');
                        for (Agent seed : config.getSeedNodes()) {
                            sb.append(seed.getListIP().get(0)).append(",");
                        }
                        sb.replace(sb.toString().length() - 1, sb.toString().length(), "");
                        sb.append('"');
                        po.addLog("Settings seeds " + sb.toString());

//                        Task setSeeds = taskRunner.executeTaskNWait(Tasks.configureCassandra(nodes, "seeds " + sb.toString()));

                        Command setSeedsCommand = Commands.getConfigureCommand(config.getNodes(), "seeds " + sb.toString());
                        commandRunner.runCommand(setSeedsCommand);

                        if (setSeedsCommand.hasSucceeded()) {
                            po.addLog("Configure seeds succeeded");
                        } else {
                            po.addLogFailed(String.format("Installation failed, %s", setSeedsCommand.getAllErrors()));
                            return;
                        }
                        po.addLogDone("Installation of Cassandra cluster succeeded");


                    } else {
                        //destroy all lxcs also
                        try {
                            lxcManager.destroyLxcs(config.getNodes());
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
    }

    @Override
    public UUID checkNode(String clusterName, String lxcHostname) {
        return null;
    }

    public UUID startAllNodes(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Task startTask = taskRunner.executeTaskNWait(Tasks.getStartAllNodesTask(config.getNodes()));

                if (startTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone("Start succeeded");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", startTask.getFirstError()));
                }

            }
        });

        return po.getId();
    }

    public UUID stopAllNodes(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Task stopTask = taskRunner.executeTaskNWait(Tasks.getStopAllNodesTask(config.getNodes()));

                if (stopTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone("Stop succeeded");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", stopTask.getFirstError()));
                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID checkAllNodes(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Task checkStatusTask = taskRunner.executeTaskNWait(Tasks.getCheckAllNodesTask(config.getNodes()));

                if (checkStatusTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone("Check succeeded");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", checkStatusTask.getFirstError()));
                }

            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {

        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

    }


}
