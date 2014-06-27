package org.safehaus.subutai.impl.manager;

import org.safehaus.subutai.shared.protocol.Agent;

public class TemplateManagerImpl extends TemplateManagerBase {

    @Override
    public Agent clone(String hostName, String templateName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return clone(a, templateName, cloneName);
    }

    @Override
    public boolean cloneDestroy(String hostName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.CLONE_DESTROY, cloneName);
    }

    @Override
    public boolean convertClone(String hostName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return scriptExecutor.execute(a, ActionType.TEMPLATE, cloneName);
    }

    private Agent clone(Agent parent, String templateName, String cloneName) {
        boolean b = scriptExecutor.execute(parent, ActionType.CLONE, templateName, cloneName);
        if(b) return agentManager.getAgentByHostname(cloneName);
        return null;
    }
}
