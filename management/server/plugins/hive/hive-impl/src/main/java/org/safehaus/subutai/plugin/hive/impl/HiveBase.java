package org.safehaus.subutai.plugin.hive.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HiveBase implements Hive {

    protected CommandRunner commandRunner;
    protected AgentManager agentManager;
    protected Tracker tracker;
    protected DbManager dbManager;
    protected ContainerManager containerManager;
    protected EnvironmentManager environmentManager;

    protected PluginDAO pluginDao;
    protected ExecutorService executor;

    static final Logger logger = LoggerFactory.getLogger(HiveImpl.class);

    public void init() {
        executor = Executors.newCachedThreadPool();
        pluginDao = new PluginDAO(dbManager);
    }

    public void destroy() {
        executor.shutdown();
    }

    public Logger getLogger() {
        return logger;
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

    public ContainerManager getContainerManager() {
        return containerManager;
    }

    public void setContainerManager(ContainerManager containerManager) {
        this.containerManager = containerManager;
    }

    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }

    public void setEnvironmentManager(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    public PluginDAO getPluginDao() {
        return pluginDao;
    }

}
