package org.safehaus.subutai.impl.hive.query;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.api.hive.query.HiveQuery;
import org.safehaus.subutai.core.tracker.api.Tracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class HiveQueryBase implements HiveQuery {

	protected static CommandRunner commandRunner;
	protected static DbManager dbManager;
	protected static Tracker tracker;
	protected static AgentManager agentManager;

	protected ExecutorService executor;

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		HiveQueryBase.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		HiveQueryImpl.tracker = tracker;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		HiveQueryBase.agentManager = agentManager;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		executor.shutdown();
	}

	public DbManager getDbManager() {
		return dbManager;
	}

	public void setDbManager(DbManager dbManager) {
		HiveQueryBase.dbManager = dbManager;
	}

	public ExecutorService getExecutor() {
		return executor;
	}
}
