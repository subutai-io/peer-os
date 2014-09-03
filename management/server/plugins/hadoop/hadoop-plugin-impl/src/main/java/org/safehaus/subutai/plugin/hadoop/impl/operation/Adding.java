package org.safehaus.subutai.plugin.hadoop.impl.operation;


import com.google.common.base.Strings;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopSetupStrategy;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.operation.common.AddNodeOperation;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;
import java.util.UUID;


/**
 * Created by daralbaev on 08.04.14.
 */
public class Adding {
	private HadoopImpl parent;
	private HadoopClusterConfig hadoopClusterConfig;
	private String clusterName;


	public Adding(HadoopImpl parent, String clusterName) {
		this.parent = parent;
		this.clusterName = clusterName;
	}


	public UUID execute() {
		final ProductOperation po =
				parent.getTracker().createProductOperation(HadoopClusterConfig.PRODUCT_KEY, "Adding node to Hadoop");

		parent.getExecutor().execute(new Runnable() {
			@Override
			public void run() {

				hadoopClusterConfig = parent.getDbManager().getInfo(HadoopClusterConfig.PRODUCT_KEY, clusterName,
						HadoopClusterConfig.class);
				if (hadoopClusterConfig == null ||
						Strings.isNullOrEmpty(hadoopClusterConfig.getClusterName()) ||
						Strings.isNullOrEmpty(hadoopClusterConfig.getDomainName())) {
					po.addLogFailed("Malformed configuration\nHadoop adding new node aborted");
					return;
				}

				try {
					po.addLog(String.format("Creating %d lxc container...", 1));
					Set<Agent> cfgServers = HadoopImpl.getContainerManager()
							.clone(hadoopClusterConfig.getTemplateName(), 3,
									HadoopImpl.getAgentManager().getPhysicalAgents(),
									HadoopSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.SLAVE_NODE ));
					Agent agent = null;

					for (Agent a : cfgServers) {
						agent = a;
					}
					po.addLog("Lxc containers created successfully\nConfiguring network...");

					if (parent.getNetworkManager().configHostsOnAgents(hadoopClusterConfig.getAllNodes(), agent,
							hadoopClusterConfig.getDomainName()) && parent.getNetworkManager().configSshOnAgents(
							hadoopClusterConfig.getAllNodes(), agent)) {
						po.addLog("Cluster network configured");

						AddNodeOperation addOperation = new AddNodeOperation(hadoopClusterConfig, agent);
						for (Command command : addOperation.getCommandList()) {
							po.addLog((String.format("%s started...", command.getDescription())));
							HadoopImpl.getCommandRunner().runCommand(command);

							if (command.hasSucceeded()) {
								po.addLogDone(String.format("%s succeeded", command.getDescription()));
							} else {
								po.addLogFailed(String.format("%s failed, %s", command.getDescription(),
										command.getAllErrors()));
							}
						}

						hadoopClusterConfig.getTaskTrackers().add(agent);
						hadoopClusterConfig.getDataNodes().add(agent);

						if (parent.getDbManager()
								.saveInfo(HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
										hadoopClusterConfig)) {
							po.addLog("Cluster info saved to DB");
						} else {
							po.addLogFailed("Could not save cluster info to DB! Please see logs\n"
									+ "Adding new node aborted");
						}
					} else {
						po.addLogFailed("Could not configure network! Please see logs\nLXC creation aborted");
					}
				} catch (LxcCreateException ex) {
					po.addLogFailed(ex.getMessage());
				}
			}
		});

		return po.getId();
	}
}
