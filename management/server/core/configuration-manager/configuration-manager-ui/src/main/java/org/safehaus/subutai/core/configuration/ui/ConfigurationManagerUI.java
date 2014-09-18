package org.safehaus.subutai.core.configuration.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConfigurationManagerUI implements PortalModule {

    public final String MODULE_IMAGE = "config.png";
    public final String MODULE_NAME = "Configuration";
    private ExecutorService executor;
    private ConfigManager configManager;
    //    private AgentManager agentManager;


    public ExecutorService getExecutor() {
        return executor;
    }


    //    public void setAgentManager( AgentManager agentManager ) {
    //        this.agentManager = agentManager;
    //    }


    public ConfigManager getConfigManager() {
        return configManager;
    }


    public void setConfigManager( final ConfigManager configManager ) {
        this.configManager = configManager;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        this.configManager = null;
        executor.shutdown();
    }


    @Override
    public String getId() {
        return MODULE_NAME;
    }


    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new ConfigurationManagerForm( configManager );
    }

    @Override
    public Boolean isCorePlugin() {
        return true;
    }
}
