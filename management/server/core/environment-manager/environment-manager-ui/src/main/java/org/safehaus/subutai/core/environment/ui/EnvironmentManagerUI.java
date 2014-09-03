package org.safehaus.subutai.core.environment.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class EnvironmentManagerUI implements PortalModule {

	public static final String MODULE_IMAGE = "env.png";
	public static final String MODULE_NAME = "Environment";
	private static ExecutorService executor;
	private EnvironmentManager environmentManager;
	//    private AgentManager agentManager;


	public static ExecutorService getExecutor() {
		return executor;
	}


	//    public void setAgentManager( AgentManager agentManager ) {
	//        this.agentManager = agentManager;
	//    }


	public void setEnvironmentManager(final EnvironmentManager environmentManager) {
		this.environmentManager = environmentManager;
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
		return FileUtil.getFile(EnvironmentManagerUI.MODULE_IMAGE, this);
	}


	@Override
	public Component createComponent() {
		return new EnvironmentManagerForm(environmentManager);
	}
}
