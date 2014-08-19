package org.safehaus.subutai.plugin.accumulo.impl;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * This is an accumulo cluster setup strategy over existing Hadoop & ZK clusters
 */
public class AccumuloOverZkNHadoopSetupStrategy implements ClusterSetupStrategy {


	private final AccumuloImpl accumuloManager;
	private final ProductOperation po;
	private final HadoopClusterConfig hadoopClusterConfig;
	private final ZookeeperClusterConfig zookeeperClusterConfig;
	private final AccumuloClusterConfig accumuloClusterConfig;


	public AccumuloOverZkNHadoopSetupStrategy(final ProductOperation po,
	                                          final AccumuloClusterConfig accumuloClusterConfig,
	                                          final HadoopClusterConfig hadoopClusterConfig,
	                                          final ZookeeperClusterConfig zookeeperClusterConfig,
	                                          final AccumuloImpl accumuloManager) {

		Preconditions.checkNotNull(hadoopClusterConfig, "Hadoop cluster config is null");
		Preconditions.checkNotNull(zookeeperClusterConfig, "ZK cluster config is null");
		Preconditions.checkNotNull(accumuloClusterConfig, "Accumulo cluster config is null");
		Preconditions.checkNotNull(po, "Product operation tracker is null");
		Preconditions.checkNotNull(accumuloManager, "Accumulo manager is null");

		this.po = po;
		this.accumuloClusterConfig = accumuloClusterConfig;
		this.hadoopClusterConfig = hadoopClusterConfig;
		this.zookeeperClusterConfig = zookeeperClusterConfig;
		this.accumuloManager = accumuloManager;
	}


	@Override
	public AccumuloClusterConfig setup() throws ClusterSetupException {
		if (accumuloClusterConfig.getMasterNode() == null || accumuloClusterConfig.getGcNode() == null
				|| accumuloClusterConfig.getMonitor() == null || Strings
				.isNullOrEmpty(accumuloClusterConfig.getClusterName()) || Util
				.isCollectionEmpty(accumuloClusterConfig.getTracers()) || Util
				.isCollectionEmpty(accumuloClusterConfig.getSlaves())) {
			throw new ClusterSetupException("Malformed configuration");
		}

		if (accumuloManager.getCluster(accumuloClusterConfig.getClusterName()) != null) {
			throw new ClusterSetupException(
					String.format("Cluster with name '%s' already exists", accumuloClusterConfig.getClusterName()));
		}

		if (accumuloManager.getHadoopManager().getCluster(hadoopClusterConfig.getClusterName()) == null) {
			throw new ClusterSetupException(
					String.format("Hadoop cluster with name '%s' not found", hadoopClusterConfig.getClusterName()));
		}
		if (accumuloManager.getZkManager().getCluster(zookeeperClusterConfig.getClusterName()) == null) {
			throw new ClusterSetupException(
					String.format("ZK cluster with name '%s' not found", zookeeperClusterConfig.getClusterName()));
		}


		if (!hadoopClusterConfig.getAllNodes().containsAll(accumuloClusterConfig.getAllNodes())) {
			throw new ClusterSetupException(String.format("Not all supplied nodes belong to Hadoop cluster %s",
					hadoopClusterConfig.getClusterName()));
		}


		if (!zookeeperClusterConfig.getNodes().containsAll(accumuloClusterConfig.getAllNodes())) {
			throw new ClusterSetupException(String.format("Not all supplied nodes belong to Zookeeper cluster %s",
					zookeeperClusterConfig.getClusterName()));
		}


		po.addLog("Checking prerequisites...");

		//check installed subutai packages
		Command checkInstalledCommand = Commands.getCheckInstalledCommand(accumuloClusterConfig.getAllNodes());
		accumuloManager.getCommandRunner().runCommand(checkInstalledCommand);

		if (!checkInstalledCommand.hasCompleted()) {
			throw new ClusterSetupException("Failed to check presence of installed subutai packages");
		}

		for (Agent node : accumuloClusterConfig.getAllNodes()) {
			AgentResult result = checkInstalledCommand.getResults().get(node.getUuid());

			if (result.getStdOut().contains(Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_NAME)) {
				throw new ClusterSetupException(
						String.format("Node %s already has Accumulo installed", node.getHostname()));
			} else if (!result.getStdOut().contains(Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME)) {
				throw new ClusterSetupException(
						String.format("Node %s has no Hadoop installation", node.getHostname()));
			} else if (!result.getStdOut().contains(Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME)) {
				throw new ClusterSetupException(
						String.format("Node %s has no Zookeeper installation", node.getHostname()));
			}
		}


		po.addLog("Installing Accumulo...");

		//install
		Command installCommand = Commands.getInstallCommand(accumuloClusterConfig.getAllNodes());
		accumuloManager.getCommandRunner().runCommand(installCommand);

		if (installCommand.hasSucceeded()) {
			po.addLog("Installation succeeded\nSetting master node...");

			Command setMasterCommand = Commands.getAddMasterCommand(accumuloClusterConfig.getAllNodes(),
					accumuloClusterConfig.getMasterNode());
			accumuloManager.getCommandRunner().runCommand(setMasterCommand);

			if (setMasterCommand.hasSucceeded()) {
				po.addLog("Setting master node succeeded\nSetting GC node...");
				Command setGCNodeCommand = Commands.getAddGCCommand(accumuloClusterConfig.getAllNodes(),
						accumuloClusterConfig.getGcNode());
				accumuloManager.getCommandRunner().runCommand(setGCNodeCommand);
				if (setGCNodeCommand.hasSucceeded()) {
					po.addLog("Setting GC node succeeded\nSetting monitor node...");

					Command setMonitorCommand = Commands.getAddMonitorCommand(accumuloClusterConfig.getAllNodes(),
							accumuloClusterConfig.getMonitor());
					accumuloManager.getCommandRunner().runCommand(setMonitorCommand);

					if (setMonitorCommand.hasSucceeded()) {
						po.addLog("Setting monitor node succeeded\nSetting tracers...");

						Command setTracersCommand = Commands.getAddTracersCommand(accumuloClusterConfig.getAllNodes(),
								accumuloClusterConfig.getTracers());
						accumuloManager.getCommandRunner().runCommand(setTracersCommand);

						if (setTracersCommand.hasSucceeded()) {
							po.addLog("Setting tracers succeeded\nSetting slaves...");

							Command setSlavesCommand =
									Commands.getAddSlavesCommand(accumuloClusterConfig.getAllNodes(),
											accumuloClusterConfig.getSlaves());
							accumuloManager.getCommandRunner().runCommand(setSlavesCommand);

							if (setSlavesCommand.hasSucceeded()) {
								po.addLog("Setting slaves succeeded\nSetting ZK cluster...");

								Command setZkClusterCommand =
										Commands.getBindZKClusterCommand(accumuloClusterConfig.getAllNodes(),
												zookeeperClusterConfig.getNodes());
								accumuloManager.getCommandRunner().runCommand(setZkClusterCommand);

								if (setZkClusterCommand.hasSucceeded()) {
									po.addLog("Setting ZK cluster succeeded\nInitializing cluster with HDFS...");

									Command initCommand =
											Commands.getInitCommand(accumuloClusterConfig.getInstanceName(),
													accumuloClusterConfig.getPassword(),
													accumuloClusterConfig.getMasterNode());
									accumuloManager.getCommandRunner().runCommand(initCommand);

									if (initCommand.hasSucceeded()) {
										po.addLog("Initialization succeeded\nStarting cluster...");

										Command startClusterCommand =
												Commands.getStartCommand(accumuloClusterConfig.getMasterNode());
										accumuloManager.getCommandRunner().runCommand(startClusterCommand);

										if (startClusterCommand.hasSucceeded()) {
											po.addLog("Cluster started successfully");
										} else {
											po.addLog(String.format("Starting cluster failed, %s, skipping...",
													startClusterCommand.getAllErrors()));
										}

										po.addLog("Updating db...");
										if (accumuloManager.getDbManager().saveInfo(AccumuloClusterConfig.PRODUCT_KEY,
												accumuloClusterConfig.getClusterName(), accumuloClusterConfig)) {
											po.addLog("Cluster info saved to DB");
										} else {
											throw new ClusterSetupException(
													"Could not save cluster info to DB! Please see logs");
										}
									} else {
										throw new ClusterSetupException(String.format("Initialization failed, %s",
												initCommand.getAllErrors()));
									}
								} else {
									throw new ClusterSetupException(String.format("Setting ZK cluster failed, %s",
											setZkClusterCommand.getAllErrors()));
								}
							} else {
								throw new ClusterSetupException(
										String.format("Setting slaves failed, %s", setSlavesCommand.getAllErrors()));
							}
						} else {
							throw new ClusterSetupException(
									String.format("Setting tracers failed, %s", setTracersCommand.getAllErrors()));
						}
					} else {
						throw new ClusterSetupException(
								String.format("Setting monitor failed, %s", setMonitorCommand.getAllErrors()));
					}
				} else {
					throw new ClusterSetupException(
							String.format("Setting gc node failed, %s", setGCNodeCommand.getAllErrors()));
				}
			} else {
				throw new ClusterSetupException(
						String.format("Setting master node failed, %s", setMasterCommand.getAllErrors()));
			}
		} else {
			throw new ClusterSetupException(
					String.format("Installation failed, %s", installCommand.getAllErrors()));
		}


		return accumuloClusterConfig;
	}
}
