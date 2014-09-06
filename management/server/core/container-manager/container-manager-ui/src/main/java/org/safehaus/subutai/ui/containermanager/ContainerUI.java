package org.safehaus.subutai.ui.containermanager;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.containermanager.ContainerManager;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContainerUI implements PortalModule {

	public static final String MODULE_IMAGE = "lxc.png";
	public static final String MODULE_NAME = "Container";
	private static ExecutorService executor;
    private static ExecutorService agentExecutor;
    private static CompletionService completionService;
	private AgentManager agentManager;
	private ContainerManager containerManager;


	public static ExecutorService getExecutor() {
		return executor;
	}

    public static ExecutorService getAgentExecutor() {
        return agentExecutor;
    }

    public static CompletionService getCompletionService() {
        return completionService;
    }

    public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setContainerManager(ContainerManager containerManager) {
		this.containerManager = containerManager;
	}


	public void init() {
		executor = Executors.newFixedThreadPool(3);
        agentExecutor = Executors.newCachedThreadPool();
        completionService = new ExecutorCompletionService(agentExecutor);
	}


	public void destroy() {
		executor.shutdown();
        agentExecutor.shutdown();
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
		return FileUtil.getFile(ContainerUI.MODULE_IMAGE, this);
	}


	@Override
	public Component createComponent() {
		return new ContainerForm(agentManager, containerManager);
	}
}
