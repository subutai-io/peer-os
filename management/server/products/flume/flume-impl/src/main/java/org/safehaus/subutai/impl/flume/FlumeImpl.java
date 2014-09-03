package org.safehaus.subutai.impl.flume;

import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.api.flume.Flume;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.impl.flume.handler.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlumeImpl implements Flume {

	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private Tracker tracker;
	private DbManager dbManager;

	private ExecutorService executor;

	public FlumeImpl(CommandRunner commandRunner, AgentManager agentManager, Tracker tracker, DbManager dbManager) {
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.tracker = tracker;
		this.dbManager = dbManager;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		executor.shutdown();
	}

	public CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public DbManager getDbManager() {
		return dbManager;
	}

	@Override
	public UUID installCluster(final Config config) {
		AbstractOperationHandler h = new InstallHandler(this, config);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID uninstallCluster(final String clusterName) {
		AbstractOperationHandler h = new UninstallHandler(this, clusterName);
		executor.execute(h);
		return h.getTrackerId();
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
	public UUID startNode(final String clusterName, final String hostname) {
		AbstractOperationHandler h = new StartHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID stopNode(final String clusterName, final String hostname) {
		AbstractOperationHandler h = new StopHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID checkNode(final String clusterName, final String hostname) {
		AbstractOperationHandler h = new StatusHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID addNode(final String clusterName, final String hostname) {
		AbstractOperationHandler h = new AddNodeHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

	@Override
	public UUID destroyNode(final String clusterName, final String hostname) {
		AbstractOperationHandler h = new DestroyNodeHandler(this, clusterName, hostname);
		executor.execute(h);
		return h.getTrackerId();
	}

}
