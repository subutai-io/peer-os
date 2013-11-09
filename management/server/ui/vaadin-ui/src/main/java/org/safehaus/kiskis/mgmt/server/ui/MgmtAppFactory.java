package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import org.safehaus.kiskis.mgmt.server.ui.bridge.ApplicationFactory;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

public class MgmtAppFactory implements ApplicationFactory {

    private final String title;
    private ModuleService moduleService;
    private AgentManagerInterface agentManagerService;

    public MgmtAppFactory(String title) {
        this.title = title;
    }

    @Override
    public String getApplicationCSSClassName() {
        return "MgmtApplication";
    }

    @Override
    public SystemMessages getSystemMessages() {
        return null;
    }

    @Override
    public Application newInstance() {
        return new MgmtApplication(title, this.moduleService, this.agentManagerService);
    }

    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    public void setAgentManagerService(AgentManagerInterface agentManagerService) {
        this.agentManagerService = agentManagerService;
    }
}