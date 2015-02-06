package org.safehaus.subutai.core.environment.terminal.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class EnvironmentTerminalPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "env_terminal.png";
    public static final String MODULE_NAME = "Environment Terminal";
    private EnvironmentManager environmentManager;


    public EnvironmentTerminalPortalModule( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void init()
    {
        // while empty
    }


    public void destroy()
    {
        // while empty
    }


    @Override
    public String getId()
    {
        return EnvironmentTerminalPortalModule.MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return EnvironmentTerminalPortalModule.MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( EnvironmentTerminalPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TerminalForm( environmentManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
