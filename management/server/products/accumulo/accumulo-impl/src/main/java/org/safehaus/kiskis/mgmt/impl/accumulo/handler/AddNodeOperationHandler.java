package org.safehaus.kiskis.mgmt.impl.accumulo.handler;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.kiskis.mgmt.api.accumulo.NodeType;
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
public class AddNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;
    private final String lxcHostname;
    private final NodeType nodeType;

    public AddNodeOperationHandler(AccumuloImpl manager, String clusterName, String lxcHostname, NodeType nodeType) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        this.nodeType = nodeType;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node %s of type %s to %s", lxcHostname, nodeType, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        if (Strings.isNullOrEmpty(clusterName) || Strings.isNullOrEmpty(lxcHostname) || nodeType == null) {
            po.addLogFailed("Malformed arguments passed");
            return;
        }
        if (!(nodeType == NodeType.TRACER || nodeType.isSlave())) {
            po.addLogFailed("Only tracer or slave node can be added");
            return;
        }
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        Agent lxcAgent = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (lxcAgent == null) {
            po.addLogFailed(String.format("Agent %s is not connected\nOperation aborted", lxcHostname));
            return;
        }

        if (nodeType == NodeType.TRACER && config.getTracers().contains(lxcAgent)) {
            po.addLogFailed(String.format("Agent %s already belongs to tracers\nOperation aborted", lxcHostname));
            return;
        } else if (nodeType.isSlave() && config.getSlaves().contains(lxcAgent)) {
            po.addLogFailed(String.format("Agent %s already belongs to slaves\nOperation aborted", lxcHostname));
            return;
        }

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(lxcAgent));
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if (!checkInstalledCommand.hasCompleted()) {
            po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get(lxcAgent.getUuid());

        if (!result.getStdOut().contains("ksks-hadoop")) {
            po.addLogFailed(String.format("Node %s has no Hadoop installation. Installation aborted", lxcAgent.getHostname()));
            return;
        }
        boolean install = !result.getStdOut().contains("ksks-accumulo");

        org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopConfig = manager.getHadoopManager().getCluster(config.getClusterName());

        if (hadoopConfig == null) {
            po.addLogFailed(String.format("Hadoop cluster with name '%s' not found\nInstallation aborted", config.getClusterName()));
            return;
        }

        if (!hadoopConfig.getAllNodes().contains(lxcAgent)) {
            po.addLogFailed(String.format("Node '%s' does not belong to Hadoop cluster %s\nInstallation aborted", lxcAgent.getHostname(), config.getClusterName()));
            return;
        }

        org.safehaus.kiskis.mgmt.api.zookeeper.Config zkConfig = manager.getZkManager().getCluster(config.getZkClusterName());

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
        if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

            po.addLog("Cluster info updated in DB");

            if (install) {
                po.addLog(String.format("Installing %s on %s node...", Config.PRODUCT_KEY, lxcAgent.getHostname()));

                Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(lxcAgent));
                manager.getCommandRunner().runCommand(installCommand);

                if (installCommand.hasSucceeded()) {
                    po.addLog("Installation succeeded");
                } else {
                    po.addLogFailed(String.format("Installation failed, %s\nOperation aborted",
                            installCommand.getAllErrors()));
                    return;
                }
            }
            po.addLog("Registering node with cluster...");

            Command addNodeCommand;
            if (nodeType.isSlave()) {
                addNodeCommand = Commands.getAddSlavesCommand(config.getAllNodes(), Util.wrapAgentToSet(lxcAgent));
            } else {
                addNodeCommand = Commands.getAddTracersCommand(config.getAllNodes(), Util.wrapAgentToSet(lxcAgent));
            }
            manager.getCommandRunner().runCommand(addNodeCommand);

            if (addNodeCommand.hasSucceeded()) {

                po.addLog("Node registration succeeded\nSetting Zk cluster...");

                Command setZkClusterCommand = Commands.getBindZKClusterCommand(Util.wrapAgentToSet(lxcAgent), zkConfig.getNodes());
                manager.getCommandRunner().runCommand(setZkClusterCommand);

                if (setZkClusterCommand.hasSucceeded()) {
                    po.addLog("Setting ZK cluster succeeded\nRestarting cluster...");

                    Command restartClusterCommand = Commands.getRestartCommand(config.getMasterNode());
                    manager.getCommandRunner().runCommand(restartClusterCommand);

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
            po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
        }
    }
}
