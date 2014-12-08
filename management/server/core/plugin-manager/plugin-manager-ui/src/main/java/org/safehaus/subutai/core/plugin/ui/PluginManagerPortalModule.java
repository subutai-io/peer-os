package org.safehaus.subutai.core.plugin.ui;



import java.io.File;
import com.vaadin.ui.Component;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.plugin.api.PluginManager;
import org.safehaus.subutai.server.ui.api.PortalModule;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "plug.png";
    public static final String MODULE_NAME = "plugins";
    private PluginManager pluginManager;

    public PluginManager getPluginManager()
    {
        return pluginManager;
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
        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
