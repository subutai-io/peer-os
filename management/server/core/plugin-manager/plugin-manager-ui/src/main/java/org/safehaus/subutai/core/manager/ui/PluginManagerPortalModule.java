package org.safehaus.subutai.core.manager.ui;



import java.io.File;
import com.vaadin.ui.Component;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.manager.api.PluginManager;
import org.safehaus.subutai.server.ui.api.PortalModule;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "plugs.png";
    public static final String MODULE_NAME = "Plugin";
    private PluginManager pluginManager;

    public PluginManagerPortalModule( PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public PluginManager getPluginManager()
    {
        return pluginManager;
    }
    public void setPluginManager( final PluginManager pluginManager ) {
        this.pluginManager = pluginManager;
    }

    public void init()
    {

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
        return new PluginManagerComponent( this, pluginManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
