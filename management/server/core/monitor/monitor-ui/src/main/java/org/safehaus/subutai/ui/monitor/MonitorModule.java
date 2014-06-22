package org.safehaus.subutai.ui.monitor;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.monitor.Monitor;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;

public class MonitorModule implements PortalModule {

	public static final String MODULE_IMAGE = "monitor.png";
	private static final String MODULE_NAME = "Monitor";

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
		FileUtil.writeFile(MonitorModule.MODULE_IMAGE, this);
		return FileUtil.getFile(MonitorModule.MODULE_IMAGE, this);
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
