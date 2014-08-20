package org.safehaus.subutai.plugin.zookeeper.impl;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.*;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.shared.protocol.NodeGroup;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//TODO: Add parameter validation
public class ZookeeperImpl implements Zookeeper {

	private final CommandRunner commandRunner;
	private final AgentManager agentManager;
	private final DbManager dbManager;
	private final Tracker tracker;
	private final ContainerManager containerManager;
	private final EnvironmentManager environmentManager;
	private final Hadoop hadoopManager;
	private ExecutorService executor;


	public ZookeeperImpl(final CommandRunner commandRunner, final AgentManager agentManager, final DbManager dbManager,
	                     final Tracker tracker, final ContainerManager containerManager,
	                     final EnvironmentManager environmentManager, final Hadoop hadoopManager) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.containerManager = containerManager;
		this.environmentManager = environmentManager;
		this.hadoopManager = hadoopManager;

		Commands.init(commandRunner);
	}


	public EnvironmentManager getEnvironmentManager() {
		return environmentManager;
	}


	public Hadoop getHadoopManager() {
		return hadoopManager;
	}


	public CommandRunner getCommandRunner() {
		return commandRunner;
	}


	public AgentManager getAgentManager() {
		return agentManager;
	}


	public DbManager getDbManager() {
		return dbManager;
	}


	public Tracker getTracker() {
		return tracker;
	}


	public ContainerManager getContainerManager() {
		return containerManager;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	public UUID installCluster(ZookeeperClusterConfig config) {

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID uninstallCluster(String clusterName) {

		AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public List<ZookeeperClusterConfig> getClusters() {

		return dbManager.getInfo(ZookeeperClusterConfig.PRODUCT_KEY, ZookeeperClusterConfig.class);
	}

	@Override
	public ZookeeperClusterConfig getCluster(String clusterName) {
		return dbManager.getInfo(ZookeeperClusterConfig.PRODUCT_KEY, clusterName, ZookeeperClusterConfig.class);
	}

	public UUID installCluster(ZookeeperClusterConfig config, HadoopClusterConfig hadoopClusterConfig) {

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config, hadoopClusterConfig);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID startNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new StartNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID stopNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new StopNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID checkNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID addNode(String clusterName) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID addNode(String clusterName, String lxcHostname) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID destroyNode(String clusterName, String lxcHostName) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public UUID addProperty(String clusterName, String fileName, String propertyName, String propertyValue) {

		AbstractOperationHandler operationHandler =
				new AddPropertyOperationHandler(this, clusterName, fileName, propertyName, propertyValue);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public UUID removeProperty(String clusterName, String fileName, String propertyName) {

		AbstractOperationHandler operationHandler =
				new RemovePropertyOperationHandler(this, clusterName, fileName, propertyName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public ClusterSetupStrategy getClusterSetupStrategy(final Environment environment,
	                                                    final ZookeeperClusterConfig config,
	                                                    final ProductOperation po) {
		if (config.getSetupType() == SetupType.STANDALONE) {
			//this is a standalone ZK cluster setup
			return new ZookeeperStandaloneSetupStrategy(environment, config, po, this);
		} else if (config.getSetupType() == SetupType.WITH_HADOOP) {
			//this is a with-Hadoop ZK cluster setup
			return new ZookeeperWithHadoopSetupStrategy(environment, config, po, this);
		} else {
			//this is an over-Hadoop ZK cluster setup
			return new ZookeeperOverHadoopSetupStrategy(config, po, this);
		}
	}

	public EnvironmentBlueprint getDefaultEnvironmentBlueprint(ZookeeperClusterConfig config) {


		EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
		environmentBlueprint.setName(String.format("%s-%s", ZookeeperClusterConfig.PRODUCT_KEY, UUID.randomUUID()));

		//node group
		NodeGroup nodesGroup = new NodeGroup();
		nodesGroup.setName("DEFAULT");
		nodesGroup.setNumberOfNodes(config.getNumberOfNodes());
		nodesGroup.setTemplateName(config.getTemplateName());
		nodesGroup.setPlacementStrategy(ZookeeperStandaloneSetupStrategy.getNodePlacementStrategy());


		environmentBlueprint.setNodeGroups(Sets.newHashSet(nodesGroup));

		return environmentBlueprint;
	}
}
