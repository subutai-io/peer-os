package org.safehaus.subutai.plugin.storm.impl;


import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class StormBase implements Storm
{

    private static final Logger LOG = LoggerFactory.getLogger( StormImpl.class.getName() );
    protected Tracker tracker;
    protected Zookeeper zookeeperManager;
    protected EnvironmentManager environmentManager;

    protected PluginDAO pluginDAO;
    protected ExecutorService executor;
    protected DataSource dataSource;


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDAO( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }

        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Zookeeper getZookeeperManager()
    {
        return zookeeperManager;
    }


    public void setZookeeperManager( Zookeeper zookeeperManager )
    {
        this.zookeeperManager = zookeeperManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public Logger getLogger()
    {
        return LOG;
    }
}
