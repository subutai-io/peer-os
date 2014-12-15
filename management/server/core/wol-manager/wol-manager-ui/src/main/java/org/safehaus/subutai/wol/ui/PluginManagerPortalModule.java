package org.safehaus.subutai.wol.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.wol.api.PluginManager;

import com.vaadin.ui.Component;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "plugs.png";
    public static final String MODULE_NAME = "Plugin";
    private PluginManager pluginManager;
    private ExecutorService executor;
    private Tracker tracker;


    public void setPluginManager( final PluginManager pluginManager )
    {
        this.pluginManager = pluginManager;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
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
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new PluginManagerComponent( executor, this, pluginManager, tracker );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
