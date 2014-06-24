package org.safehaus.subutai.impl.template;

import java.util.Set;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;

public class TemplateManagerImpl extends TemplateManagerBase {

    @Override
    public Agent clone(String hostName, String templateName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        boolean b = scriptExecutor.execute(a, ActionType.CLONE, templateName, cloneName);
        return b ? a : null;
    }

    @Override
    public Set<Agent> clone(Set<String> hostNames, String templateName, String cloneName) {
        return clone(hostNames, templateName, cloneName, null);
    }

    @Override
    public Set<Agent> clone(Set<String> hostNames, String templateName, String cloneName, PlacementStrategyENUM strategy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

}
