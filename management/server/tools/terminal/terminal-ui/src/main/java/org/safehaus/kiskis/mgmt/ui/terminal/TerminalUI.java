package org.safehaus.kiskis.mgmt.ui.terminal;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class TerminalUI implements Module {

    public static final String MODULE_NAME = "Terminal";
    private TaskRunner taskRunner;
    private AgentManager agentManager;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void init() {
    }

    public void destroy() {
    }

    @Override
    public String getName() {
        return TerminalUI.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new TerminalForm(taskRunner, agentManager);
    }

}
