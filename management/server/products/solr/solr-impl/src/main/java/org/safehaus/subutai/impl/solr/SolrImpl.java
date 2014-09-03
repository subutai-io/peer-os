package org.safehaus.subutai.impl.solr;

import com.google.common.base.Preconditions;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.api.solr.Solr;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.solr.handler.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SolrImpl implements Solr {

	protected Commands commands;
	protected AgentManager agentManager;
	protected DbManager dbManager;
	protected LxcManager lxcManager;
	private CommandRunner commandRunner;
	private Tracker tracker;
	private ExecutorService executor;


	public SolrImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
	                LxcManager lxcManager) {
		this.commands = new Commands(commandRunner);
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
		this.lxcManager = lxcManager;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
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


	public LxcManager getLxcManager() {
		return lxcManager;
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

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	public UUID startNode(final String clusterName, final String lxcHostName) {

		AbstractOperationHandler operationHandler = new StartNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID stopNode(final String clusterName, final String lxcHostName) {

		AbstractOperationHandler operationHandler = new StopNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	public UUID checkNode(final String clusterName, final String lxcHostName) {

		AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID addNode(final String clusterName) {

		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(this, clusterName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}

	public UUID destroyNode(final String clusterName, final String lxcHostName) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostName);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}
}
