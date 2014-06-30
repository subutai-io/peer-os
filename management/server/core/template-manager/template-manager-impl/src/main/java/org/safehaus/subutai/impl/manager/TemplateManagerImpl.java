package org.safehaus.subutai.impl.manager;

import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.shared.protocol.Agent;

public class TemplateManagerImpl extends TemplateManagerBase {

    @Override
    public boolean clone(String hostName, String templateName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.CLONE, templateName, cloneName);
    }

    @Override
    public boolean cloneDestroy(String hostName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.CLONE_DESTROY, cloneName);
    }

    @Override
    public boolean promoteClone(String hostName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.TEMPLATE, cloneName);
    }

    @Override
    public boolean importTemplate(String hostName, String templateName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.IMPORT, templateName);
    }

    @Override
    public boolean exportTemplate(String hostName, String templateName) {
        // check if registered as template
        Template template = templateRegistry.getTemplate(templateName);
        if(template == null) return false;

        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.EXPORT, templateName);
    }

}
