package org.safehaus.subutai.hadoop.impl;


import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.hadoop.api.Config;
import org.safehaus.subutai.hadoop.api.Hadoop;
import org.safehaus.subutai.hadoop.api.NodeType;
import org.safehaus.subutai.hadoop.impl.operation.common.InstallHadoopOperation;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import java.util.Set;


/**
 * This is a mongodb cluster setup strategy.
 */
public class HadoopDbSetupStrategy implements ClusterSetupStrategy {

	private Hadoop hadoopManager;
	private ContainerManager containerManager;
	private ProductOperation po;
	private Config config;
	public static final String TEMPLATE_NAME = "hadoop";


	/*@todo add parameter validation logic*/
	public HadoopDbSetupStrategy(ProductOperation po, Hadoop hadoopManager, ContainerManager containerManager,
	                             Config config) {
		this.hadoopManager = hadoopManager;
		this.containerManager = containerManager;
		this.po = po;
		this.config = config;
	}


	public static PlacementStrategyENUM getNodePlacementStrategyByNodeType(NodeType nodeType) {
		switch (nodeType) {
			case MASTER_NODE:
				return PlacementStrategyENUM.MORE_RAM;
			case SLAVE_NODE:
				return PlacementStrategyENUM.MORE_HDD;
			default:
				return PlacementStrategyENUM.ROUND_ROBIN;
		}
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
		po.addLog("Destroying lxc containers");
		try {
			for (Agent agent : config.getAllNodes()) {
				HadoopImpl.getContainerManager().cloneDestroy(agent.getParentHostName(), agent.getHostname());
			}
			po.addLog("Lxc containers successfully destroyed");
		} catch (LxcDestroyException ex) {
			po.addLog(String.format("%s, skipping...", ex.getMessage()));
		}
		po.addLogFailed(log);
	}


	@Override
	public Config setup() throws ClusterSetupException {

		if (config == null ||
				Strings.isNullOrEmpty(config.getClusterName()) ||
				Strings.isNullOrEmpty(config.getDomainName())) {
			po.addLogFailed("Malformed configuration\nHadoop installation aborted");
		} else {
			//check if mongo cluster with the same name already exists
			if (hadoopManager.getCluster(config.getClusterName()) != null) {
				po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted",
						config.getClusterName()));
			} else {
				try {
					po.addLog(String.format("Creating %d master servers...", 3));
					Set<Agent> cfgServers = containerManager
							.clone(TEMPLATE_NAME, 3, HadoopImpl.getAgentManager().getPhysicalAgents(),
									getNodePlacementStrategyByNodeType(NodeType.MASTER_NODE));

					po.addLog(String.format("Creating %d slave nodes...", config.getCountOfSlaveNodes()));
					Set<Agent> dataNodes = containerManager
							.clone(TEMPLATE_NAME, config.getCountOfSlaveNodes(), HadoopImpl.getAgentManager().getPhysicalAgents(),
									getNodePlacementStrategyByNodeType(NodeType.SLAVE_NODE));

					setMasterNodes(cfgServers);
					setSlaveNodes(dataNodes);

					po.addLog("Lxc containers created successfully");

					//continue installation here

					installHadoopCluster();

					//@todo add containers destroyal in case of failure
				} catch (LxcCreateException ex) {
					po.addLogFailed(ex.getMessage());
				}
			}
		}

		return config;
	}


	private void installHadoopCluster() throws ClusterSetupException {

		if (HadoopImpl.getNetworkManager().configHostsOnAgents(config.getAllNodes(), config.getDomainName()) &&
				HadoopImpl.getNetworkManager().configSshOnAgents(config.getAllNodes())) {
			po.addLog("Cluster network configured");

			po.addLog("Hadoop installation started");

			InstallHadoopOperation installOperation = new InstallHadoopOperation(config);
			for (Command command : installOperation.getCommandList()) {
				po.addLog((String.format("%s started...", command.getDescription())));
				HadoopImpl.getCommandRunner().runCommand(command);

				if (command.hasSucceeded()) {
					po.addLogDone(String.format("%s succeeded", command.getDescription()));
				} else {
					po.addLogFailed(String.format("%s failed, %s", command.getDescription(), command.getAllErrors()));
				}
			}

			if (HadoopImpl.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
				po.addLog("Cluster info saved to DB");
			} else {
				destroyLXC(po, "Could not save cluster info to DB! Please see logs\nInstallation aborted");
			}
		} else {
			destroyLXC(po, "Could not configure network! Please see logs\nLXC creation aborted");
		}
	}
}
