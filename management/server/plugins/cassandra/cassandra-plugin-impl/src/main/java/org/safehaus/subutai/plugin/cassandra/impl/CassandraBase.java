package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;


/**
 * Created by bahadyr on 9/4/14.
 */
abstract class CassandraBase {

    DbManager dbManager;
    Tracker tracker;
    LxcManager lxcManager;
    ExecutorService executor;
    NetworkManager networkManager;
    CommandRunner commandRunner;
    AgentManager agentManager;
    EnvironmentManager environmentManager;
    ContainerManager containerManager;
    PluginDAO pluginDAO;


    public DbManager getDbManager() {
        return dbManager;
    }


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( final Tracker tracker ) {
        this.tracker = tracker;
    }


    public LxcManager getLxcManager() {
        return lxcManager;
    }


    public void setLxcManager( final LxcManager lxcManager ) {
        this.lxcManager = lxcManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public void setExecutor( final ExecutorService executor ) {
        this.executor = executor;
    }


    public NetworkManager getNetworkManager() {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager ) {
        this.networkManager = networkManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager ) {
        this.containerManager = containerManager;
    }


    public PluginDAO getPluginDAO() {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDAO pluginDAO ) {
        this.pluginDAO = pluginDAO;
    }
}
