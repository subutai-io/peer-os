package org.safehaus.subutai.core.container.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LxcUI implements PortalModule {

	public static final String MODULE_IMAGE = "lxc.png";
	public static final String MODULE_NAME = "LXC";
	private static ExecutorService executor;
	private AgentManager agentManager;
	private LxcManager lxcManager;


	public static ExecutorService getExecutor() {
		return executor;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
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
		return FileUtil.getFile(LxcUI.MODULE_IMAGE, this);
	}


	@Override
	public Component createComponent() {
		return new LxcForm(agentManager, lxcManager);
	}
}
