package org.safehaus.subutai.impl.hadoop.operation;

import com.google.common.base.Strings;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
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
	private HadoopClusterConfig hadoopClusterConfig;

	public Installation(HadoopImpl parent, HadoopClusterConfig hadoopClusterConfig ) {
		this.parent = parent;
		this.hadoopClusterConfig = hadoopClusterConfig;
	}

	public UUID execute() {
		final ProductOperation po = parent.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY, "Installation of Hadoop");

		parent.getExecutor().execute(new Runnable() {
			@Override
			public void run() {

				if ( hadoopClusterConfig == null ||
						Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName()) ||
						Strings.isNullOrEmpty( hadoopClusterConfig.getDomainName())) {
					po.addLogFailed("Malformed configuration\nHadoop installation aborted");
					return;
				}

				//check if mongo cluster with the same name already exists
				if (parent.getCluster( hadoopClusterConfig.getClusterName()) != null) {
					po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted",
							hadoopClusterConfig.getClusterName()));
					return;
				}

				try {
					po.addLog(String.format("Creating %d lxc containers...", hadoopClusterConfig.getCountOfSlaveNodes() + 3));
					Map<String, Set<Agent>> nodes = CustomPlacementStrategy.getNodes(
							parent.getLxcManager(), 3, hadoopClusterConfig.getCountOfSlaveNodes());

					setMasterNodes(nodes.get(CustomPlacementStrategy.MASTER_NODE_TYPE));
					setSlaveNodes(nodes.get(CustomPlacementStrategy.SLAVE_NODE_TYPE));
					po.addLog("Lxc containers created successfully\nConfiguring network...");

					if (parent.getNetworkManager().configHostsOnAgents( hadoopClusterConfig.getAllNodes(), hadoopClusterConfig
                            .getDomainName())
							&& parent.getNetworkManager().configSshOnAgents( hadoopClusterConfig.getAllNodes())) {

						po.addLog("Cluster network configured");
						po.addLog("Hadoop installation started");

						if (parent.getDbManager().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                                hadoopClusterConfig )) {
							po.addLog("Cluster info saved to DB");
						} else {
							destroyLXC(po, "Could not save cluster info to DB! Please see logs\nInstallation aborted");
						}

						InstallHadoopOperation installOperation = new InstallHadoopOperation( hadoopClusterConfig );

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
								hadoopClusterConfig.getClusterName()));
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
			hadoopClusterConfig.setNameNode(arr[0]);
			hadoopClusterConfig.setJobTracker(arr[1]);
			hadoopClusterConfig.setSecondaryNameNode(arr[2]);
		}
	}

	private void setSlaveNodes(Set<Agent> agents) {
		if (agents != null) {
			hadoopClusterConfig.getDataNodes().addAll(agents);
			hadoopClusterConfig.getTaskTrackers().addAll(agents);
		}
	}

	private void destroyLXC(ProductOperation po, String log) {
		//destroy all lxcs also
		try {
			parent.getLxcManager().destroyLxcs(new HashSet<>( hadoopClusterConfig.getAllNodes()));
			if (parent.getDbManager().deleteInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName())) {
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
