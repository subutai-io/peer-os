package org.safehaus.subutai.impl.lxcmanager;

import java.util.*;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.lxcmanager.*;
import org.safehaus.subutai.api.manager.TemplateManager;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.impl.strategy.PlacementStrategyFactory;
import org.safehaus.subutai.shared.protocol.Agent;

public class ContainerManagerImpl implements ContainerManager {

    private LxcManager lxcManager;
    private AgentManager agentManager;
    private TemplateManager templateManager;

    public LxcManager getLxcManager() {
        return lxcManager;
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void init() {
    }

    public void destroy() {
    }

    @Override
    public Set<Agent> clone(Collection<String> hostNames, String templateName, int nodesCount, PlacementStrategyENUM... strategy) {
        LxcPlacementStrategy st = PlacementStrategyFactory.create(nodesCount, strategy);
        Map<Agent, Integer> slots = st.calculateSlots(lxcManager.getPhysicalServerMetrics());

        int n = 1;
        List<String> cloneNames = new ArrayList<>();
        for(Map.Entry<Agent, Integer> e : slots.entrySet()) {
            for(int i = 0; i < e.getValue(); i++) {
                String name = nextHostName(templateName, n);
                boolean b = templateManager.clone(e.getKey().getHostname(),
                        templateName, name);
                if(b) cloneNames.add(name);
            }
        }
        // get agents by names
        Set<Agent> clones = new HashSet<>();
        for(String cloneName : cloneNames) {
            Agent a = agentManager.getAgentByHostname(cloneName);
            if(a != null) clones.add(a);
        }
        return clones;
    }

    private String nextHostName(String templateName, int offset) {
        while(true) {
            String name = templateName + "-" + offset++;
            Agent a = agentManager.getAgentByHostname(name);
            if(a == null) return name;
        }
    }

}
