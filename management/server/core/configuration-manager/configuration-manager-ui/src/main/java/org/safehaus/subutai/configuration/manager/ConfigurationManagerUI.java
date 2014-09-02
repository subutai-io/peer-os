package org.safehaus.subutai.configuration.manager;


import com.vaadin.ui.Component;
import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConfigurationManagerUI implements PortalModule {

	public static final String MODULE_IMAGE = "config.png";
	public static final String MODULE_NAME = "Configuration";
	private static ExecutorService executor;
	private ConfigManager configManager;
	//    private AgentManager agentManager;


	public static ExecutorService getExecutor() {
		return executor;
	}


	//    public void setAgentManager( AgentManager agentManager ) {
	//        this.agentManager = agentManager;
	//    }


	public ConfigManager getConfigManager() {
		return configManager;
	}


	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
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
		return FileUtil.getFile(ConfigurationManagerUI.MODULE_IMAGE, this);
	}


	@Override
	public Component createComponent() {
		return new ConfigurationManagerForm(configManager);
	}
}
