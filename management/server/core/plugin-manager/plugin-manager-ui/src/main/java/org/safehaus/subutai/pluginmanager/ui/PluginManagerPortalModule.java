package org.safehaus.subutai.pluginmanager.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.pluginmanager.api.PluginManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;


public class PluginManagerPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "plugs.png";
    public static final String MODULE_NAME = "Plugin";
    private PluginManager pluginManager;
    private ExecutorService executor;
    private Tracker tracker;


    public PluginManagerPortalModule( final PluginManager pluginManager, final Tracker tracker )
    {
        Preconditions.checkNotNull( pluginManager );
        Preconditions.checkNotNull( tracker );

        this.pluginManager = pluginManager;
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
