package org.safehaus.subutai.impl.accumulo.handler;


import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.api.accumulo.NodeType;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.impl.accumulo.AccumuloImpl;
import org.safehaus.subutai.impl.accumulo.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;

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
			po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
			return;
		}

		Agent lxcAgent = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (lxcAgent == null) {
			po.addLogFailed(String.format("Agent %s is not connected", lxcHostname));
			return;
		}

		if (nodeType == NodeType.TRACER && config.getTracers().contains(lxcAgent)) {
			po.addLogFailed(String.format("Agent %s already belongs to tracers", lxcHostname));
			return;
		} else if (nodeType.isSlave() && config.getSlaves().contains(lxcAgent)) {
			po.addLogFailed(String.format("Agent %s already belongs to slaves", lxcHostname));
			return;
		}

		//check installed subutai packages
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(Sets.newHashSet(lxcAgent));
		manager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			po.addLogFailed("Failed to check presence of installed subutai packages");
			return;
		}

		AgentResult result = checkInstalledCommand.getResults().get(lxcAgent.getUuid());

		if (!result.getStdOut().contains("ksks-hadoop")) {
			po.addLogFailed(String.format("Node %s has no Hadoop installation.", lxcAgent.getHostname()));
			return;
		} else if (!result.getStdOut().contains("ksks-zookeeper")) {
			po.addLogFailed(String.format("Node %s has no Zookeeper installation.", lxcAgent.getHostname()));
			return;
		}

		boolean install = !result.getStdOut().contains("ksks-accumulo");

		HadoopClusterConfig hadoopHadoopClusterConfig =
				manager.getHadoopManager().getCluster(config.getClusterName());

		if ( hadoopHadoopClusterConfig == null) {
			po.addLogFailed(String.format("Hadoop cluster with name '%s' not found", config.getClusterName()));
			return;
		}

		if (!hadoopHadoopClusterConfig.getAllNodes().contains(lxcAgent)) {
			po.addLogFailed(String.format("Node '%s' does not belong to Hadoop cluster %s", lxcAgent.getHostname(),
					config.getClusterName()));
			return;
		}

		org.safehaus.subutai.api.zookeeper.Config zkConfig =
				manager.getZkManager().getCluster(config.getClusterName());

		if (zkConfig == null) {
			po.addLogFailed(String.format("Zookeeper cluster with name '%s' not found", config.getClusterName()));
			return;
		}

		if (!zkConfig.getNodes().contains(lxcAgent)) {
			po.addLogFailed(String.format("Node '%s' does not belong to Zookeeper cluster %s", lxcAgent.getHostname(),
					config.getClusterName()));
			return;
		}


		if (nodeType.isSlave()) {
			config.getSlaves().add(lxcAgent);
		} else {
			config.getTracers().add(lxcAgent);
		}


		if (install) {
			po.addLog(String.format("Installing %s on %s node...", Config.PRODUCT_KEY, lxcAgent.getHostname()));

			Command installCommand = Commands.getInstallCommand(Sets.newHashSet(lxcAgent));
			manager.getCommandRunner().runCommand(installCommand);

			if (installCommand.hasSucceeded()) {
				po.addLog("Installation succeeded");
			} else {
				po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
				return;
			}
		}
		po.addLog("Registering node with cluster...");

		Command addNodeCommand;
		if (nodeType.isSlave()) {
			addNodeCommand = Commands.getAddSlavesCommand(config.getAllNodes(), config.getSlaves());
		} else {
			addNodeCommand = Commands.getAddTracersCommand(config.getAllNodes(), config.getTracers());
		}
		manager.getCommandRunner().runCommand(addNodeCommand);

		if (addNodeCommand.hasSucceeded()) {
			po.addLog("Node registration succeeded\nSetting master node...");

			Command setMasterNodeCommand =
					Commands.getAddMasterCommand(Sets.newHashSet(lxcAgent), config.getMasterNode());
			manager.getCommandRunner().runCommand(setMasterNodeCommand);

			if (setMasterNodeCommand.hasSucceeded()) {

				po.addLog("Setting master node succeeded\nSetting GC node...");

				Command setGcNodeCommand =
						Commands.getAddGCCommand(Sets.newHashSet(lxcAgent), config.getGcNode());
				manager.getCommandRunner().runCommand(setGcNodeCommand);

				if (setGcNodeCommand.hasSucceeded()) {

					po.addLog("Setting GC node succeeded\nSetting monitor node...");

					Command setMonitorCommand =
							Commands.getAddMonitorCommand(Sets.newHashSet(lxcAgent), config.getMonitor());
					manager.getCommandRunner().runCommand(setMonitorCommand);

					if (setMonitorCommand.hasSucceeded()) {

						po.addLog("Setting monitor node succeeded\nSetting tracers/slaves...");

						Command setTracersSlavesCommand = nodeType.isSlave() ? Commands.getAddTracersCommand(
								Sets.newHashSet(lxcAgent), config.getTracers()) :
								Commands.getAddSlavesCommand(Sets.newHashSet(lxcAgent),
										config.getSlaves());

						manager.getCommandRunner().runCommand(setTracersSlavesCommand);

						if (setTracersSlavesCommand.hasSucceeded()) {

							po.addLog("Setting tracers/slaves succeeded\nSetting Zk cluster...");

							Command setZkClusterCommand =
									Commands.getBindZKClusterCommand(Sets.newHashSet(lxcAgent),
											zkConfig.getNodes());
							manager.getCommandRunner().runCommand(setZkClusterCommand);

							if (setZkClusterCommand.hasSucceeded()) {
								po.addLog("Setting ZK cluster succeeded\nRestarting cluster...");

								Command restartClusterCommand = Commands.getRestartCommand(config.getMasterNode());
								manager.getCommandRunner().runCommand(restartClusterCommand);

								//  temporarily turning off until exit code ir fixed:  if ( restartClusterCommand.hasSucceeded() ) {
								if (restartClusterCommand.hasCompleted()) {
									po.addLog("Cluster restarted successfully");
								} else {
									po.addLog(String.format("Cluster restart failed, %s, skipping...",
											restartClusterCommand.getAllErrors()));
								}

								po.addLog("Updating db...");

								try {
									manager.getDbManager()
											.saveInfo2(Config.PRODUCT_KEY, config.getClusterName(), config);

									po.addLogDone("Database information updated");
								} catch (DBException e) {
									po.addLogFailed(String.format("Failed to update database information, %s",
											e.getMessage()));
								}
							} else {
								po.addLogFailed(String.format("Setting ZK cluster failed, %s",
										setZkClusterCommand.getAllErrors()));
							}
						} else {
							po.addLogFailed(String.format("Setting tracers/slaves failed, %s",
									setTracersSlavesCommand.getAllErrors()));
						}
					} else {
						po.addLogFailed(
								String.format("Setting monitor node failed, %s", setMonitorCommand.getAllErrors()));
					}
				} else {
					po.addLogFailed(String.format("Setting GC node failed, %s", setGcNodeCommand.getAllErrors()));
				}
			} else {
				po.addLogFailed(
						String.format("Setting master node failed, %s", setMasterNodeCommand.getAllErrors()));
			}
		} else {
			po.addLogFailed(String.format("Node registration failed, %s", addNodeCommand.getAllErrors()));
		}
	}
}
