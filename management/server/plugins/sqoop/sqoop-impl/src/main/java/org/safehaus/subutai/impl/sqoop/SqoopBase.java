package org.safehaus.subutai.impl.sqoop;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.sqoop.Sqoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;

public abstract class SqoopBase implements Sqoop {

	protected CommandRunner commandRunner;
	protected AgentManager agentManager;
	protected Tracker tracker;
	protected DbManager dbManager;
	protected Hadoop hadoopManager;

	protected ExecutorService executor;

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		executor.shutdown();
	}

	public CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		this.commandRunner = commandRunner;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public DbManager getDbManager() {
		return dbManager;
	}

	public void setDbManager(DbManager dbManager) {
		this.dbManager = dbManager;
	}

	public Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public void setHadoopManager(Hadoop hadoopManager) {
		this.hadoopManager = hadoopManager;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

}
