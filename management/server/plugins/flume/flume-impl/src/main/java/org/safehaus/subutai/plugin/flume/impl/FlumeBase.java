package org.safehaus.subutai.plugin.flume.impl;

import java.util.concurrent.ExecutorService;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlumeBase {

    static final Logger logger = LoggerFactory.getLogger(FlumeImpl.class);

    CommandRunner commandRunner;
    AgentManager agentManager;
    Tracker tracker;
    DbManager dbManager;
    PluginDAO pluginDao;
    EnvironmentManager environmentManager;
    ContainerManager containerManager;
    Hadoop hadoopManager;

    ExecutorService executor;

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

    public PluginDAO getPluginDao() {
        return pluginDao;
    }

    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }

    public void setEnvironmentManager(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    public ContainerManager getContainerManager() {
        return containerManager;
    }

    public void setContainerManager(ContainerManager containerManager) {
        this.containerManager = containerManager;
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

    public Logger getLogger() {
        return logger;
    }
}
