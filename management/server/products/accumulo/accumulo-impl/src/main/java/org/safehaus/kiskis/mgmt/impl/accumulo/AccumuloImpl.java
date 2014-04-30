package org.safehaus.kiskis.mgmt.impl.accumulo;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.accumulo.Accumulo;
import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.kiskis.mgmt.api.accumulo.NodeType;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccumuloImpl implements Accumulo {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private Hadoop hadoopManager;
    private ExecutorService executor;


    public AccumuloImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker, Hadoop hadoopManager) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.hadoopManager = hadoopManager;

        Commands.init(commandRunner);
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public UUID installCluster(final Config config) {

        final ProductOperation po = tracker.createProductOperation(Config.PRODUCT_KEY, "Installing Accumulo");

        executor.execute(new Runnable() {

            public void run() {

                if (config == null
                        || config.getMasterNode() == null
                        || config.getGcNode() == null
                        || config.getMonitor() == null
                        || Strings.isNullOrEmpty(config.getClusterName())
                        || Strings.isNullOrEmpty(config.getZkClusterName())
                        || Util.isCollectionEmpty(config.getTracers())
                        || Util.isCollectionEmpty(config.getSlaves())
                        ) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopConfig = hadoopManager.getCluster(config.getClusterName());

                if (hadoopConfig == null) {
                    po.addLogFailed(String.format("Hadoop cluster with name '%s' not found\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (!hadoopConfig.getAllNodes().containsAll(config.getAllNodes())) {
                    po.addLogFailed(String.format("Not all supplied nodes belong to Hadoop cluster %s \nInstallation aborted", config.getClusterName()));
                    return;
                }


                org.safehaus.kiskis.mgmt.api.zookeeper.Config zkConfig = dbManager.getInfo(org.safehaus.kiskis.mgmt.api.zookeeper.Config.PRODUCT_KEY, config.getZkClusterName(), org.safehaus.kiskis.mgmt.api.zookeeper.Config.class);

                if (zkConfig == null) {
                    po.addLogFailed(String.format("Zookeeper cluster with name '%s' not found\nInstallation aborted", config.getZkClusterName()));
                    return;
                }

                po.addLog("Checking prerequisites...");

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getAllNodes());
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                for (Agent node : config.getAllNodes()) {
                    AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

                    if (result.getStdOut().contains("ksks-accumulo")) {
                        po.addLogFailed(String.format("Node %s already has Accumulo installed. Installation aborted", node.getHostname()));
                        return;
                    } else if (!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLogFailed(String.format("Node %s has no Hadoop installation. Installation aborted", node.getHostname()));
                        return;
                    }
                }

                po.addLog("Updating db...");
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

                    po.addLog("Cluster info saved to DB\nInstalling Accumulo...");

                    //install
                    Command installCommand = Commands.getInstallCommand(config.getAllNodes());
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded\nSetting master node...");

                        Command setMasterCommand = Commands.getAddMasterCommand(config.getAllNodes(), config.getMasterNode());
                        commandRunner.runCommand(setMasterCommand);

                        if (setMasterCommand.hasSucceeded()) {
                            po.addLog("Setting master node succeeded\nSetting GC node...");
                            Command setGCNodeCommand = Commands.getAddGCCommand(config.getAllNodes(), config.getGcNode());
                            commandRunner.runCommand(setGCNodeCommand);
                            if (setGCNodeCommand.hasSucceeded()) {
                                po.addLog("Setting GC node succeeded\nSetting monitor node...");

                                Command setMonitorCommand = Commands.getAddMonitorCommand(config.getAllNodes(), config.getMonitor());
                                commandRunner.runCommand(setMonitorCommand);

                                if (setMonitorCommand.hasSucceeded()) {
                                    po.addLog("Setting monitor node succeeded\nSetting tracers...");

                                    Command setTracersCommand = Commands.getAddTracersCommand(config.getAllNodes(), config.getTracers());
                                    commandRunner.runCommand(setTracersCommand);

                                    if (setTracersCommand.hasSucceeded()) {
                                        po.addLog("Setting tracers succeeded\nSetting slaves...");

                                        Command setSlavesCommand = Commands.getAddSlavesCommand(config.getAllNodes(), config.getSlaves());
                                        commandRunner.runCommand(setSlavesCommand);

                                        if (setSlavesCommand.hasSucceeded()) {
                                            po.addLog("Setting slaves succeeded\nSetting ZK cluster...");

                                            Command setZkClusterCommand = Commands.getBindZKClusterCommand(config.getAllNodes(), zkConfig.getNodes());
                                            commandRunner.runCommand(setZkClusterCommand);

                                            if (setZkClusterCommand.hasSucceeded()) {
                                                po.addLog("Setting ZK cluster succeeded\nStarting cluster...");

                                                Command startClusterCommand = Commands.getStartCommand(config.getMasterNode());
                                                commandRunner.runCommand(startClusterCommand);

                                                if (startClusterCommand.hasSucceeded()) {
                                                    po.addLogDone("Cluster started successfully\nDone");
                                                } else {
                                                    po.addLogFailed(String.format("Starting cluster failed, %s", startClusterCommand.getAllErrors()));
                                                }
                                            } else {
                                                po.addLogFailed(String.format("Setting ZK cluster failed, %s", setZkClusterCommand.getAllErrors()));
                                            }

                                        } else {
                                            po.addLogFailed(String.format("Setting slaves failed, %s", setSlavesCommand.getAllErrors()));
                                        }

                                    } else {
                                        po.addLogFailed(String.format("Setting tracers failed, %s", setTracersCommand.getAllErrors()));
                                    }

                                } else {
                                    po.addLogFailed(String.format("Setting monitor failed, %s", setMonitorCommand.getAllErrors()));
                                }

                            } else {
                                po.addLogFailed(String.format("Setting gc node failed, %s", setGCNodeCommand.getAllErrors()));
                            }
                        } else {
                            po.addLogFailed(String.format("Setting master node failed, %s", setMasterCommand.getAllErrors()));
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
                String.format("Uninstalling cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                po.addLog("Uninstalling cluster...");

                Command uninstallCommand = Commands.getUninstallCommand(config.getAllNodes());
                commandRunner.runCommand(uninstallCommand);

                if (uninstallCommand.hasCompleted()) {
                    if (uninstallCommand.hasSucceeded()) {
                        po.addLog("Cluster successfully uninstalled");
                    } else {
                        po.addLog(String.format("Uninstallation failed, %s, skipping...", uninstallCommand.getAllErrors()));
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

    public UUID startCluster(final String clusterName) {
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

                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Master node '%s' is not connected\nOperation aborted", config.getMasterNode().getHostname()));
                    return;
                }

                po.addLog("Starting cluster...");

                Command startCommand = Commands.getStartCommand(config.getMasterNode());
                commandRunner.runCommand(startCommand);

                if (startCommand.hasSucceeded()) {
                    po.addLogDone("Cluster started successfully");
                } else {
                    po.addLogFailed(String.format("Failed to start cluster %s, %s",
                            clusterName, startCommand.getAllErrors()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID stopCluster(final String clusterName) {
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

                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Master node '%s' is not connected\nOperation aborted", config.getMasterNode().getHostname()));
                    return;
                }

                po.addLog("Stopping cluster...");

                Command stopCommand = Commands.getStopCommand(config.getMasterNode());
                commandRunner.runCommand(stopCommand);

                if (stopCommand.hasSucceeded()) {
                    po.addLogDone("Cluster stopped successfully");
                } else {
                    po.addLogFailed(String.format("Failed to stop cluster %s, %s",
                            clusterName, stopCommand.getAllErrors()
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
                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostName, clusterName));
                    return;
                }

                Command checkNodeCommand = Commands.getStatusCommand(node);
                commandRunner.runCommand(checkNodeCommand);

                if (checkNodeCommand.hasSucceeded()) {
                    po.addLogDone(String.format("Status on %s is %s",
                            lxcHostName, checkNodeCommand.getResults().get(node.getUuid()).getStdOut()
                    ));
                } else {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            lxcHostName, checkNodeCommand.getAllErrors()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostName, final NodeType nodeType) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s on %s", nodeType, lxcHostName));

        executor.execute(new Runnable() {

            public void run() {
                if (!(nodeType == NodeType.TRACER || nodeType.isSlave())) {
                    po.addLogFailed("Only tracer or slave node can be destroyed");
                    return;
                }

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
                if (!config.getAllNodes().contains(agent)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostName, clusterName));
                    return;
                }
                if (agentManager.getAgentByHostname(config.getMasterNode().getHostname()) == null) {
                    po.addLogFailed(String.format("Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname()));
                    return;
                }

                if (nodeType == NodeType.TRACER) {
                    config.getTracers().remove(agent);
                } else {
                    config.getSlaves().remove(agent);
                }

                boolean uninstall = !config.getAllNodes().contains(agent);

                if (uninstall) {
                    po.addLog("Uninstalling Accumulo...");

                    Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
                    commandRunner.runCommand(uninstallCommand);

                    if (uninstallCommand.hasSucceeded()) {
                        po.addLog("Accumulo uninstallation succeeded");
                    } else {
                        po.addLog(String.format("Accumulo uninstallation failed, %s, skipping...", uninstallCommand.getAllErrors()));
                    }
                }

                Command unregisterNodeCommand;
                if (nodeType == NodeType.TRACER) {
                    unregisterNodeCommand = Commands.getClearTracerCommand(config.getAllNodes(), agent);
                } else {
                    unregisterNodeCommand = Commands.getClearSlaveCommand(config.getAllNodes(), agent);
                }

                po.addLog("Unregistering node from cluster...");
                commandRunner.runCommand(unregisterNodeCommand);

                if (unregisterNodeCommand.hasSucceeded()) {
                    po.addLog("Node unregistered successfully\nRestarting cluster...");

                    Command restartClusterCommand = Commands.getRestartCommand(config.getMasterNode());
                    commandRunner.runCommand(restartClusterCommand);
                    if (restartClusterCommand.hasSucceeded()) {
                        po.addLog("Cluster restarted successfully");
                    } else {
                        po.addLog(String.format("Cluster restart failed, %s, skipping...", restartClusterCommand.getAllErrors()));
                    }

                    po.addLog("Updating db...");
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLogDone("Cluster info updated\nDone");
                    } else {
                        po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
                                config.getClusterName()));
                    }
                } else {
                    po.addLogFailed(String.format("Unregistering node failed, %s\nOperation aborted", unregisterNodeCommand.getAllErrors()));

                }


            }
        });

        return po.getId();
    }

    public UUID addNode(final String clusterName, final String lxcHostname, final NodeType nodeType) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node %s of type %s to %s", lxcHostname, nodeType, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if (Strings.isNullOrEmpty(clusterName) || Strings.isNullOrEmpty(lxcHostname) || nodeType == null) {
                    po.addLogFailed("Malformed arguments passed");
                    return;
                }
                if (!(nodeType == NodeType.TRACER || nodeType.isSlave())) {
                    po.addLogFailed("Only tracer or slave node can be added");
                    return;
                }
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Agent lxcAgent = agentManager.getAgentByHostname(lxcHostname);
                if (lxcAgent == null) {
                    po.addLogFailed(String.format("Agent %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                if (config.getAllNodes().contains(lxcAgent)) {
                    po.addLogFailed(String.format("Agent %s already belongs to this cluster\nOperation aborted", lxcHostname));
                    return;
                }

                //check installed ksks packages
                Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(lxcAgent));
                commandRunner.runCommand(checkInstalledCommand);

                if (!checkInstalledCommand.hasCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                AgentResult result = checkInstalledCommand.getResults().get(lxcAgent.getUuid());

                if (result.getStdOut().contains("ksks-accumulo")) {
                    po.addLogFailed(String.format("Node %s already has Accumulo installed. Installation aborted", lxcAgent.getHostname()));
                    return;
                } else if (!result.getStdOut().contains("ksks-hadoop")) {
                    po.addLogFailed(String.format("Node %s has no Hadoop installation. Installation aborted", lxcAgent.getHostname()));
                    return;
                }

                org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopConfig = hadoopManager.getCluster(config.getClusterName());

                if (hadoopConfig == null) {
                    po.addLogFailed(String.format("Hadoop cluster with name '%s' not found\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (!hadoopConfig.getAllNodes().contains(lxcAgent)) {
                    po.addLogFailed(String.format("Node '%s' does not belong to Hadoop cluster %s\nInstallation aborted", lxcAgent.getHostname(), config.getClusterName()));
                    return;
                }

                org.safehaus.kiskis.mgmt.api.zookeeper.Config zkConfig = dbManager.getInfo(org.safehaus.kiskis.mgmt.api.zookeeper.Config.PRODUCT_KEY, config.getZkClusterName(), org.safehaus.kiskis.mgmt.api.zookeeper.Config.class);

                if (zkConfig == null) {
                    po.addLogFailed(String.format("Zookeeper cluster with name '%s' not found\nInstallation aborted", config.getZkClusterName()));
                    return;
                }


                if (nodeType.isSlave()) {
                    config.getSlaves().add(lxcAgent);
                } else {
                    config.getTracers().add(lxcAgent);
                }

                po.addLog("Updating DB...");
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog(String.format("Cluster info updated in DB\nInstalling %s on %s node...", Config.PRODUCT_KEY, lxcAgent.getHostname()));

                    Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(lxcAgent));
                    commandRunner.runCommand(installCommand);

                    if (installCommand.hasSucceeded()) {
                        po.addLog("Installation succeeded\nRegistering node with cluster...");

                        Command addNodeCommand;
                        if (nodeType.isSlave()) {
                            addNodeCommand = Commands.getAddSlavesCommand(config.getAllNodes(), Util.wrapAgentToSet(lxcAgent));
                        } else {
                            addNodeCommand = Commands.getAddTracersCommand(config.getAllNodes(), Util.wrapAgentToSet(lxcAgent));
                        }
                        commandRunner.runCommand(addNodeCommand);

                        if (addNodeCommand.hasSucceeded()) {

                            po.addLog("Node registration succeeded\nSetting Zk cluster...");

                            Command setZkClusterCommand = Commands.getBindZKClusterCommand(Util.wrapAgentToSet(lxcAgent), zkConfig.getNodes());
                            commandRunner.runCommand(setZkClusterCommand);

                            if (setZkClusterCommand.hasSucceeded()) {
                                po.addLog("Setting ZK cluster succeeded\nRestarting cluster...");

                                Command restartClusterCommand = Commands.getRestartCommand(config.getMasterNode());
                                commandRunner.runCommand(restartClusterCommand);

                                if (restartClusterCommand.hasSucceeded()) {
                                    po.addLogDone("Cluster restarted successfully\nDone");
                                } else {
                                    po.addLogFailed(String.format("Cluster restart failed, %s", restartClusterCommand.getAllErrors()));
                                }
                            } else {
                                po.addLogFailed(String.format("Setting ZK cluster failed, %s",
                                        setZkClusterCommand.getAllErrors()));
                            }
                        } else {
                            po.addLogFailed(String.format("Adding node failed, %s",
                                    addNodeCommand.getAllErrors()));
                        }

                    } else {
                        po.addLogFailed(String.format("Installation failed, %s",
                                installCommand.getAllErrors()));
                    }

                } else {
                    po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                }

            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {

        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);

    }

    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

}
