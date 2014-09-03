package org.safehaus.subutai.core.monitor.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.monitor.api.Monitor;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;


public class MonitoringModule implements PortalModule {

	public static final String MODULE_IMAGE = "monitoring.png";
	private static final String MODULE_NAME = "Monitoring";
	private Monitor monitor;
	private AgentManager agentManager;


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
		return FileUtil.getFile( MonitoringModule.MODULE_IMAGE, this );
	}


	@Override
	public Component createComponent() {
		return new ModuleView(monitor, agentManager);
	}


	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}
}
