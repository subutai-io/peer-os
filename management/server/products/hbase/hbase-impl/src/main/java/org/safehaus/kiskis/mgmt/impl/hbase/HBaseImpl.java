package org.safehaus.kiskis.mgmt.impl.hbase;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseType;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

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

                    // Installing HBase
                    Task installTask = taskRunner.executeTaskNWait(Tasks.getInstallTask(allNodes));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Installation success..");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installTask.getFirstError()));
                        return;
                    }

                    po.addLog("Installation succeeded\nConfiguring master...");

                    // Configuring master
                    Task configMasterTask = taskRunner.executeTaskNWait(Tasks.getConfigMasterTask(allNodes, config.getHadoopNameNode(), config.getMaster()));

                    if (configMasterTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Configure master success...");
                    } else {
                        po.addLogFailed(String.format("Configuration failed, %s", configMasterTask.getFirstError()));
                        return;
                    }
                    po.addLog("Configuring master succeeded\nConfiguring region...");

                    // Configuring region
                    Task configRegionTask = taskRunner.executeTaskNWait(Tasks.getConfigRegionTask(allNodes, config.getRegion()));

                    if (configRegionTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Configuring region success...");
                    } else {
                        po.addLogFailed(String.format("Configuring failed, %s", configRegionTask.getFirstError()));
                        return;
                    }
                    po.addLog("Configuring region succeeded\nSetting quorum...");

                    // Configuring quorum
                    Task configQuorumTask = taskRunner.executeTaskNWait(Tasks.getConfigQuorumTask(allNodes, config.getQuorum()));

                    if (configQuorumTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLog("Configuring quorum success...");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", configQuorumTask.getFirstError()));
                        return;
                    }
                    po.addLog("Setting quorum succeeded\nSetting backup masters...");

                    // Configuring backup master
                    Task configBackupMastersTask = taskRunner.executeTaskNWait(Tasks.getConfigBackupMastersTask(allNodes, config.getBackupMasters()));

                    if (configBackupMastersTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLogDone("Configuring backup master success...");
                    } else {
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

                Task uninstallTask = taskRunner.executeTaskNWait(Tasks.getUninstallTask(allNodes));

                if (uninstallTask.getTaskStatus() != TaskStatus.SUCCESS) {
                    po.addLogFailed(String.format("Uninstallation failed, %s", uninstallTask.getFirstError()));
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

    public List<HBaseConfig> getClusters() {

        return dbManager.getInfo(HBaseConfig.PRODUCT_KEY, HBaseConfig.class);

    }

    @Override
    public UUID startCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Starting cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Set<Agent> nodes = new HashSet<Agent>();
                nodes.addAll(config.getQuorum());
                nodes.addAll(config.getRegion());
                nodes.add(config.getBackupMasters());
                nodes.add(config.getMaster());

                final Task startNodeTask = new Task("Start HBase nodes");

                for (Iterator<Agent> iterator = nodes.iterator(); iterator.hasNext(); ) {
                    Agent agent = iterator.next();
                    if (agentManager.getAgentByHostname(agent.getHostname()) == null) {
                        po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                        return;
                    }
                    po.addLog(agent.getHostname() + " " + Commands.getStatusCommand());
                    startNodeTask.addRequest(Commands.getStartCommand(), agent);
                }

                taskRunner.executeTaskNWait(startNodeTask);

                if (startNodeTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone("Starting all nodes done");
                } else {
                    po.addLogFailed(String.format("Starting all nodes failed %s", startNodeTask.getFirstError()));

                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID stopCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Stopping cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Set<Agent> nodes = new HashSet<Agent>();
                nodes.addAll(config.getQuorum());
                nodes.addAll(config.getRegion());
                nodes.add(config.getBackupMasters());
                nodes.add(config.getMaster());

                final Task stopNodeTask = new Task("Stop HBase nodes");

                for (Iterator<Agent> iterator = nodes.iterator(); iterator.hasNext(); ) {
                    Agent agent = iterator.next();
                    if (agentManager.getAgentByHostname(agent.getHostname()) == null) {
                        po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                        return;
                    }
                    po.addLog(agent.getHostname() + " " + Commands.getStatusCommand());
                    stopNodeTask.addRequest(Commands.getStopCommand(), agent);
                }

                taskRunner.executeTaskNWait(stopNodeTask);

                if (stopNodeTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone("Stopping all nodes done");
                } else {
                    po.addLogFailed(String.format("Starting all nodes failed %s", stopNodeTask.getFirstError()));

                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID checkNode(HBaseType type, String clusterName, String lxcHostname) {
        return null;
    }

    @Override
    public UUID startNodes(String clusterName) {
        return null;
    }

    @Override
    public UUID stopNodes(String clusterName) {
        return null;
    }

    @Override
    public UUID checkCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(HBaseConfig.PRODUCT_KEY,
                String.format("Checking cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo(HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Set<Agent> nodes = new HashSet<Agent>();
                nodes.addAll(config.getQuorum());
                nodes.addAll(config.getRegion());
                nodes.add(config.getBackupMasters());
                nodes.add(config.getMaster());

                final Task checkNodeTask = new Task("Check HBase nodes");

                for (Iterator<Agent> iterator = nodes.iterator(); iterator.hasNext(); ) {
                    Agent agent = iterator.next();
                    if (agentManager.getAgentByHostname(agent.getHostname()) == null) {
                        po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", agent.getHostname()));
                        return;
                    }
                    po.addLog(agent.getHostname() + " " + Commands.getStatusCommand());
                    checkNodeTask.addRequest(Commands.getStatusCommand(), agent);
                }

                taskRunner.executeTaskNWait(checkNodeTask);

                if (checkNodeTask.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLogDone("Checking all nodes done");
                } else {
                    po.addLogFailed(String.format("Checking all nodes failed %s", checkNodeTask.getFirstError()));

                }

            }
        });

        return po.getId();
    }

}
