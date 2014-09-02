package org.safehaus.subutai.impl.accumulo;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.api.accumulo.Accumulo;
import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.api.accumulo.NodeType;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.api.zookeeper.Zookeeper;
import org.safehaus.subutai.impl.accumulo.handler.*;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;

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
	private Zookeeper zkManager;
	private ExecutorService executor;


	public AccumuloImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
	                    Hadoop hadoopManager, Zookeeper zkManager) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.hadoopManager = hadoopManager;
		this.zkManager = zkManager;

		Commands.init(commandRunner);
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


	public Hadoop getHadoopManager() {
		return hadoopManager;
	}


	public Zookeeper getZkManager() {
		return zkManager;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	public UUID installCluster(final Config config) {

		Preconditions.checkNotNull(config, "Configuration is null");

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID uninstallCluster(final String clusterName) {

		AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public List<Config> getClusters() {

		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}


	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}


	public UUID startCluster(final String clusterName) {

		AbstractOperationHandler operationHandler = new StartClusterOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID stopCluster(final String clusterName) {

		AbstractOperationHandler operationHandler = new StopClusterOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID checkNode(final String clusterName, final String lxcHostName) {

		AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID addNode(final String clusterName, final String lxcHostname, final NodeType nodeType) {

		AbstractOperationHandler operationHandler =
				new AddNodeOperationHandler(this, clusterName, lxcHostname, nodeType);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID destroyNode(final String clusterName, final String lxcHostName, final NodeType nodeType) {

		AbstractOperationHandler operationHandler =
				new DestroyNodeOperationHandler(this, clusterName, lxcHostName, nodeType);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	@Override
	public UUID addProperty(final String clusterName, final String propertyName, final String propertyValue) {

		AbstractOperationHandler operationHandler =
				new AddPropertyOperationHandler(this, clusterName, propertyName, propertyValue);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	@Override
	public UUID removeProperty(final String clusterName, final String propertyName) {

		AbstractOperationHandler operationHandler =
				new RemovePropertyOperationHandler(this, clusterName, propertyName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}
}
