package org.safehaus.subutai.impl.hadoop.operation;

import com.google.common.base.Strings;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hadoop.HadoopImpl;
import org.safehaus.subutai.impl.hadoop.operation.common.InstallHadoopOperation;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Installation {
	private HadoopImpl parent;
	private Config config;

	public Installation(HadoopImpl parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public UUID execute() {
		final ProductOperation po = parent.getTracker().createProductOperation(Config.PRODUCT_KEY, "Installation of Hadoop");

		parent.getExecutor().execute(new Runnable() {
			@Override
			public void run() {

				if (config == null ||
						Strings.isNullOrEmpty(config.getClusterName()) ||
						Strings.isNullOrEmpty(config.getDomainName())) {
					po.addLogFailed("Malformed configuration\nHadoop installation aborted");
					return;
				}

				//check if mongo cluster with the same name already exists
				if (parent.getCluster(config.getClusterName()) != null) {
					po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted",
							config.getClusterName()));
					return;
				}

				try {
					po.addLog(String.format("Creating %d lxc containers...", config.getCountOfSlaveNodes() + 3));
					Map<String, Set<Agent>> nodes = CustomPlacementStrategy.getNodes(
							parent.getLxcManager(), 3, config.getCountOfSlaveNodes());

					setMasterNodes(nodes.get(CustomPlacementStrategy.MASTER_NODE_TYPE));
					setSlaveNodes(nodes.get(CustomPlacementStrategy.SLAVE_NODE_TYPE));
					po.addLog("Lxc containers created successfully\nConfiguring network...");

					if (parent.getNetworkManager().configHostsOnAgents(config.getAllNodes(), config.getDomainName())
							&& parent.getNetworkManager().configSshOnAgents(config.getAllNodes())) {

						po.addLog("Cluster network configured");
						po.addLog("Hadoop installation started");

						if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
							po.addLog("Cluster info saved to DB");
						} else {
							destroyLXC(po, "Could not save cluster info to DB! Please see logs\nInstallation aborted");
						}

						InstallHadoopOperation installOperation = new InstallHadoopOperation(config);

						for (Command command : installOperation.getCommandList()) {
							po.addLog((String.format("%s started...", command.getDescription())));
							HadoopImpl.getCommandRunner().runCommand(command);

							if (command.hasSucceeded()) {
								po.addLog(String.format("%s succeeded", command.getDescription()));
							} else {
								po.addLogFailed(String.format("%s failed, %s", command.getDescription(),
										command.getAllErrors()));
							}
						}

						po.addLogDone(String.format("Cluster '%s' \nInstallation finished",
								config.getClusterName()));
					} else {
						destroyLXC(po, "Could not configure network! Please see logs\nLXC creation aborted");
					}
				} catch (LxcCreateException ex) {
					po.addLogFailed(ex.getMessage());
				}
			}
		});

		return po.getId();
	}

	private void setMasterNodes(Set<Agent> agents) {
		if (agents != null && agents.size() >= 3) {
			Agent[] arr = agents.toArray(new Agent[agents.size()]);
			config.setNameNode(arr[0]);
			config.setJobTracker(arr[1]);
			config.setSecondaryNameNode(arr[2]);
		}
	}

	private void setSlaveNodes(Set<Agent> agents) {
		if (agents != null) {
			config.getDataNodes().addAll(agents);
			config.getTaskTrackers().addAll(agents);
		}
	}

	private void destroyLXC(ProductOperation po, String log) {
		//destroy all lxcs also
		try {
			parent.getLxcManager().destroyLxcs(new HashSet<>(config.getAllNodes()));
			if (parent.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
				po.addLogDone("Cluster info deleted from DB\nDone");
			} else {
				po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
			}
		} catch (LxcDestroyException ex) {
			po.addLogFailed(log + "\nUse LXC module to cleanup");
		}
		po.addLogFailed(log);
	}
}
