package org.safehaus.subutai.core.configuration.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class ConfigurationManagerUI implements PortalModule
{

    private static final String IMAGE = "config.png";
    private static final String NAME = "Configuration";
    private ExecutorService executor;
    private ConfigManager configManager;


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public ConfigManager getConfigManager()
    {
        return configManager;
    }


    public void setConfigManager( final ConfigManager configManager )
    {
        this.configManager = configManager;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        this.configManager = null;
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
        return new ConfigurationManagerForm( configManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
