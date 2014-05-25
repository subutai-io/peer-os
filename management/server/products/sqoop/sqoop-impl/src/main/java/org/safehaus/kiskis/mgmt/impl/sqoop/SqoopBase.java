package org.safehaus.kiskis.mgmt.impl.sqoop;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.sqoop.Sqoop;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

public abstract class SqoopBase implements Sqoop {

    protected CommandRunner commandRunner;
    protected AgentManager agentManager;
    protected Tracker tracker;
    protected DbManager dbManager;

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

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

}
