package org.safehaus.subutai.core.configuration.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.configuration.api.ConfigurationManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class ConfigurationManagerPortalModule implements PortalModule
{

    private static final String IMAGE = "config.png";
    private static final String NAME = "Configuration";
    private ExecutorService executor;
    private ConfigurationManager configurationManager;


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }


    public void setConfigurationManager( final ConfigurationManager configurationManager )
    {
        this.configurationManager = configurationManager;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        this.configurationManager = null;
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return NAME;
    }


    @Override
    public String getName()
    {
        return NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new ConfigurationManagerComponent( configurationManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
