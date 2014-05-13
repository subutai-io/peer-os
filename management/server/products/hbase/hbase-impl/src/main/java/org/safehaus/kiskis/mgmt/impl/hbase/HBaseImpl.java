package org.safehaus.kiskis.mgmt.impl.hbase;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.hbase.Config;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HBaseImpl implements HBase {

    private AgentManager agentManager;
    private Hadoop hadoopManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private CommandRunner commandRunner;

    public void init() {
        Commands.init(commandRunner);
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        this.hadoopManager = hadoopManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public UUID installCluster(final org.safehaus.kiskis.mgmt.api.hbase.Config config) {
        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, "Installing HBase");

        final Set<Agent> allNodes = new HashSet<Agent>();
        allNodes.add(config.getMaster());
        allNodes.addAll(config.getRegion());
        allNodes.addAll(config.getQuorum());
        allNodes.add(config.getBackupMasters());

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

                    po.addLog("Cluster info saved to DB\nInstalling HBase...");

                    // Installing HBase
                    po.addLog("Installing...");
                    Command installCommand = Commands.getInstallCommand(allNodes);
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation success..");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                        return;
                    }

                    po.addLog("Installation succeeded\nConfiguring master...");

                    // Configuring master
                    Command configureMasterCommand = Commands.getConfigMasterTask(allNodes, config.getHadoopNameNode().getHostname(), config.getMaster().getHostname());
                    commandRunner.runCommand(configureMasterCommand);

                    if (configureMasterCommand.hasSucceeded()) {
                        po.addLog("Configure master success...");
                    } else {
                        po.addLogFailed(String.format("Configuration failed, %s", configureMasterCommand));
                        return;
                    }
                    po.addLog("Configuring master succeeded\nConfiguring region...");

                    // Configuring region
                    StringBuilder sbRegion = new StringBuilder();
                    for (Agent agent : config.getRegion()) {
                        sbRegion.append(agent.getHostname());
                        sbRegion.append(" ");
                    }
                    Command configureRegionCommand = Commands.getConfigRegionCommand(allNodes, sbRegion.toString().trim());
                    commandRunner.runCommand(configureRegionCommand);

                    if (configureRegionCommand.hasSucceeded()) {
                        po.addLog("Configuring region success...");
                    } else {
                        po.addLogFailed(String.format("Configuring failed, %s", configureRegionCommand.getAllErrors()));
                        return;
                    }
                    po.addLog("Configuring region succeeded\nSetting quorum...");

                    // Configuring quorum
                    StringBuilder sbQuorum = new StringBuilder();
                    for (Agent agent : config.getQuorum()) {
                        sbQuorum.append(agent.getHostname());
                        sbQuorum.append(" ");
                    }
                    Command configureQuorumCommand = Commands.getConfigQuorumCommand(allNodes, sbQuorum.toString().trim());
                    commandRunner.runCommand(configureQuorumCommand);

                    if (configureQuorumCommand.hasSucceeded()) {
                        po.addLog("Configuring quorum success...");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", configureQuorumCommand.getAllErrors()));
                        return;
                    }
                    po.addLog("Setting quorum succeeded\nSetting backup masters...");

                    // Configuring backup master
                    Command configureBackupMasterCommand = Commands.getConfigBackupMastersCommand(allNodes, config.getBackupMasters().getHostname());
                    commandRunner.runCommand(configureBackupMasterCommand);

                    if (configureBackupMasterCommand.hasSucceeded()) {
                        po.addLogDone("Configuring backup master success...");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", configureBackupMasterCommand.getAllErrors()));
                        return;
                    }
                    po.addLogDone("Cluster installation succeeded\n");


                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(org.safehaus.kiskis.mgmt.api.hbase.Config.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                org.safehaus.kiskis.mgmt.api.hbase.Config config =
                        dbManager.getInfo(org.safehaus.kiskis.mgmt.api.hbase.Config.PRODUCT_KEY, clusterName, org.safehaus.kiskis.mgmt.api.hbase.Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }
                final Set<Agent> allNodes = new HashSet<Agent>();
                allNodes.add(config.getMaster());
                allNodes.addAll(config.getRegion());
                allNodes.addAll(config.getQuorum());
                allNodes.add(config.getBackupMasters());

                po.addLog("Uninstalling...");
                Command installCommand = Commands.getUninstallCommand(config.getNodes());
                commandRunner.runCommand(installCommand);

                if (installCommand.hasSucceeded()) {
                    po.addLog("Uninstallation success..");
                } else {
                    po.addLogFailed(String.format("Uninstallation failed, %s", installCommand.getAllErrors()));
                    return;
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

    public List<Config> getClusters() {

        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

    @Override
    public org.safehaus.kiskis.mgmt.api.hadoop.Config getHadoopCluster(String clusterName) {
        return dbManager.getInfo(org.safehaus.kiskis.mgmt.api.hadoop.Config.PRODUCT_KEY, clusterName, org.safehaus.kiskis.mgmt.api.hadoop.Config.class);
    }

    @Override
    public UUID startCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Set<Agent> nodes = new HashSet<Agent>();
                nodes.addAll(config.getQuorum());
                nodes.addAll(config.getRegion());
                nodes.add(config.getBackupMasters());
                nodes.add(config.getMaster());

                Command startCommand = Commands.getStartCommand(nodes);
                commandRunner.runCommand(startCommand);

                if (startCommand.hasSucceeded()) {
                    po.addLogDone("Start success..");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", startCommand.getAllErrors()));
                    return;
                }


            }
        });

        return po.getId();
    }

    @Override
    public UUID stopCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Set<Agent> nodes = new HashSet<Agent>();
                nodes.addAll(config.getQuorum());
                nodes.addAll(config.getRegion());
                nodes.add(config.getBackupMasters());
                nodes.add(config.getMaster());

                Command stopCommand = Commands.getStopCommand(nodes);
                commandRunner.runCommand(stopCommand);

                if (stopCommand.hasSucceeded()) {
                    po.addLogDone("Start success..");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", stopCommand.getAllErrors()));
                    return;
                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID checkCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Set<Agent> nodes = new HashSet<Agent>();
                nodes.addAll(config.getQuorum());
                nodes.addAll(config.getRegion());
                nodes.add(config.getBackupMasters());
                nodes.add(config.getMaster());

                Command checkCommand = Commands.getStatusCommand(nodes);
                commandRunner.runCommand(checkCommand);

                if (checkCommand.hasSucceeded()) {
                    po.addLogDone("All nodes are running..");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", checkCommand.getAllErrors()));
                    return;
                }

            }
        });

        return po.getId();
    }

    @Override
    public List<org.safehaus.kiskis.mgmt.api.hadoop.Config> getHadoopClusters() {
        List<org.safehaus.kiskis.mgmt.api.hadoop.Config> hadoopClusters =
                hadoopManager.getClusters();
        return hadoopClusters;
    }

}
