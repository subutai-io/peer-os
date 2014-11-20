package org.safehaus.subutai.plugin.presto.impl;


import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class PrestoBase
{

    private static final Logger LOG = LoggerFactory.getLogger( PrestoBase.class.getName() );
    Tracker tracker;
    Hadoop hadoopManager;
    EnvironmentManager environmentManager;
    ExecutorService executor;
    PluginDAO pluginDAO;
    Commands commands;
    public DataSource dataSource;


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


    public Commands getCommands()
    {
        return commands;
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


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }
}
