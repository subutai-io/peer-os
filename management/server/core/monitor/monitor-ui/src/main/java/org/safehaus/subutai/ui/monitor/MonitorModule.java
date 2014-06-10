package org.safehaus.subutai.ui.monitor;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.monitor.Monitor;
import org.safehaus.subutai.server.ui.api.PortalModule;

public class MonitorModule implements PortalModule {

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

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }

    @Override
    public Component createComponent() {
        return new ModuleView(monitor, agentManager);
    }
}
