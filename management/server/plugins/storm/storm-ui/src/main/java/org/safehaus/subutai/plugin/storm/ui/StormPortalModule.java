package org.safehaus.subutai.plugin.storm.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class StormPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "storm.png";
    protected static final Logger LOG = Logger.getLogger( StormPortalModule.class.getName() );
    private ExecutorService executor;
    private final Storm storm;
    private final Tracker tracker;
    private Zookeeper zookeeper;
    private final EnvironmentManager environmentManager;


    public StormPortalModule( Storm storm, Zookeeper zookeeper,  Tracker tracker, EnvironmentManager environmentManager)
    {
        this.storm = storm;
        this.zookeeper = zookeeper;
        this.tracker = tracker;
        this.environmentManager = environmentManager;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {

        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return StormClusterConfiguration.PRODUCT_NAME;
    }


    @Override
    public String getName()
    {
        return StormClusterConfiguration.PRODUCT_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( StormPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        try
        {
            return new StormComponent( executor, storm, zookeeper, tracker, environmentManager );
        }
        catch ( NamingException e )
        {
            LOG.severe( e.getMessage() );
        }

        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return false;
    }
}
