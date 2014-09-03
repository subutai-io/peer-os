package org.safehaus.subutai.impl.lucene;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.api.lucene.Lucene;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.impl.lucene.handler.AddNodeOperationHandler;
import org.safehaus.subutai.impl.lucene.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.impl.lucene.handler.InstallOperationHandler;
import org.safehaus.subutai.impl.lucene.handler.UninstallOperationHandler;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LuceneImpl implements Lucene {

	protected Commands commands;
	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private Hadoop hadoopManager;
	private ExecutorService executor;


	public LuceneImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
	                  Hadoop hadoopManager) {
		this.commands = new Commands(commandRunner);
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.hadoopManager = hadoopManager;
	}


	public Hadoop getHadoopManager() {
		return hadoopManager;
	}


	public Commands getCommands() {
		return commands;
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


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	@Override
	public UUID installCluster(final Config config) {

		Preconditions.checkNotNull(config, "Configuration is null");

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	@Override
	public UUID uninstallCluster(final String clusterName) {

		AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public List<Config> getClusters() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	@Override
	public UUID addNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	@Override
	public UUID destroyNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}
}
