package org.safehaus.kiskis.mgmt.impl.accumulo.handler;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.accumulo.AccumuloImpl;
import org.safehaus.kiskis.mgmt.impl.accumulo.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final Config config;
    private final ProductOperation po;

    public InstallOperationHandler(AccumuloImpl manager, Config config) {
        super(manager, config.getClusterName());
        this.config = config;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY, String.format("Installing %s", Config.PRODUCT_KEY));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        if (config.getMasterNode() == null
                || config.getGcNode() == null
                || config.getMonitor() == null
                || Strings.isNullOrEmpty(config.getClusterName())
                || Util.isCollectionEmpty(config.getTracers())
                || Util.isCollectionEmpty(config.getSlaves())
                ) {
            po.addLogFailed("Malformed configuration\nInstallation aborted");
            return;
        }

        if (manager.getCluster(config.getClusterName()) != null) {
            po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
            return;
        }

        org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopConfig = manager.getHadoopManager().getCluster(config.getClusterName());

        if (hadoopConfig == null) {
            po.addLogFailed(String.format("Hadoop cluster with name '%s' not found\nInstallation aborted", config.getClusterName()));
            return;
        }

        if (!hadoopConfig.getAllNodes().containsAll(config.getAllNodes())) {
            po.addLogFailed(String.format("Not all supplied nodes belong to Hadoop cluster %s \nInstallation aborted", config.getClusterName()));
            return;
        }


        org.safehaus.kiskis.mgmt.api.zookeeper.Config zkConfig = manager.getZkManager().getCluster(config.getClusterName());

        if (zkConfig == null) {
            po.addLogFailed(String.format("Zookeeper cluster with name '%s' not found\nInstallation aborted", config.getClusterName()));
            return;
        }

        po.addLog("Checking prerequisites...");

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(config.getAllNodes());
        manager.getCommandRunner().runCommand(checkInstalledCommand);

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
        if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

            po.addLog("Cluster info saved to DB\nInstalling Accumulo...");

            //install
            Command installCommand = Commands.getInstallCommand(config.getAllNodes());
            manager.getCommandRunner().runCommand(installCommand);

            if (installCommand.hasSucceeded()) {
                po.addLog("Installation succeeded\nSetting master node...");

                Command setMasterCommand = Commands.getAddMasterCommand(config.getAllNodes(), config.getMasterNode());
                manager.getCommandRunner().runCommand(setMasterCommand);

                if (setMasterCommand.hasSucceeded()) {
                    po.addLog("Setting master node succeeded\nSetting GC node...");
                    Command setGCNodeCommand = Commands.getAddGCCommand(config.getAllNodes(), config.getGcNode());
                    manager.getCommandRunner().runCommand(setGCNodeCommand);
                    if (setGCNodeCommand.hasSucceeded()) {
                        po.addLog("Setting GC node succeeded\nSetting monitor node...");

                        Command setMonitorCommand = Commands.getAddMonitorCommand(config.getAllNodes(), config.getMonitor());
                        manager.getCommandRunner().runCommand(setMonitorCommand);

                        if (setMonitorCommand.hasSucceeded()) {
                            po.addLog("Setting monitor node succeeded\nSetting tracers...");

                            Command setTracersCommand = Commands.getAddTracersCommand(config.getAllNodes(), config.getTracers());
                            manager.getCommandRunner().runCommand(setTracersCommand);

                            if (setTracersCommand.hasSucceeded()) {
                                po.addLog("Setting tracers succeeded\nSetting slaves...");

                                Command setSlavesCommand = Commands.getAddSlavesCommand(config.getAllNodes(), config.getSlaves());
                                manager.getCommandRunner().runCommand(setSlavesCommand);

                                if (setSlavesCommand.hasSucceeded()) {
                                    po.addLog("Setting slaves succeeded\nSetting ZK cluster...");

                                    Command setZkClusterCommand = Commands.getBindZKClusterCommand(config.getAllNodes(), zkConfig.getNodes());
                                    manager.getCommandRunner().runCommand(setZkClusterCommand);

                                    if (setZkClusterCommand.hasSucceeded()) {
                                        po.addLog("Setting ZK cluster succeeded\nStarting cluster...");

                                        Command startClusterCommand = Commands.getStartCommand(config.getMasterNode());
                                        manager.getCommandRunner().runCommand(startClusterCommand);

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
}
