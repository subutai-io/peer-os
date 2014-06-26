package org.safehaus.subutai.impl.template;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.impl.strategy.PlacementStrategyFactory;
import org.safehaus.subutai.shared.protocol.Agent;
import org.slf4j.LoggerFactory;

public class TemplateManagerImpl extends TemplateManagerBase {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TemplateManagerImpl.class);

    @Override
    public Agent clone(String hostName, String templateName, String cloneName) {
        Agent a = agentManager.getAgentByHostname(hostName);
        return clone(a, templateName, cloneName);
    }

    @Override
    public Agent clone(Set<String> hostNames, String templateName, String cloneName, PlacementStrategyENUM... strategy) {

        Map<Agent, ServerMetric> all = lxcManager.getPhysicalServerMetrics();
        Map<Agent, ServerMetric> targets = new HashMap<>();
        for(String hostname : hostNames) {
            Agent a = agentManager.getAgentByHostname(hostname);
            if(a != null && all.containsKey(a)) targets.put(a, all.get(a));
        }

        LxcPlacementStrategy st = PlacementStrategyFactory.create(strategy);
        try {
            st.calculatePlacement(targets);
            Map<Agent, Map<String, Integer>> info = st.getPlacementInfoMap();
            Iterator<Agent> it = info.keySet().iterator();
            if(it.hasNext())
                return clone(it.next(), templateName, cloneName);
            else
                logger.warn("Used placement strategy didn't give any servers");
        } catch(LxcCreateException ex) {
            logger.error("Failed to clone a template", ex);
        }
        return null;
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
