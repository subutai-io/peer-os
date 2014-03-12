package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.server.ui.bridge.ApplicationFactory;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;

public class MgmtAppFactory implements ApplicationFactory {

    private final String title;
    private AgentManager agentManager;
    private ModuleNotifier moduleNotifier;

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
        return new MgmtApplication(title, agentManager, moduleNotifier);
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setModuleNotifier(ModuleNotifier moduleNotifier) {
        this.moduleNotifier = moduleNotifier;
    }

}
