package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import org.safehaus.kiskis.mgmt.server.ui.bridge.ApplicationFactory;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;

public class MgmtAppFactory implements ApplicationFactory {

    private final String title;
    private MgmtApplication mgmtApplication;
    private AgentManager agentManager;

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
        mgmtApplication = new MgmtApplication(title, agentManager);
        return mgmtApplication;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

}
