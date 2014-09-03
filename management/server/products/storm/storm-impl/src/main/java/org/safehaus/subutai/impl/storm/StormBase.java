package org.safehaus.subutai.impl.storm;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.storm.Storm;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.api.zookeeper.Zookeeper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class StormBase implements Storm {

	protected CommandRunner commandRunner;
	protected AgentManager agentManager;
	protected Tracker tracker;
	protected DbManager dbManager;
	protected Zookeeper zookeeperManager;
	protected LxcManager lxcManager;

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

	public Zookeeper getZookeeperManager() {
		return zookeeperManager;
	}

	public void setZookeeperManager(Zookeeper zookeeperManager) {
		this.zookeeperManager = zookeeperManager;
	}

	public LxcManager getLxcManager() {
		return lxcManager;
	}

	public void setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
	}

}
