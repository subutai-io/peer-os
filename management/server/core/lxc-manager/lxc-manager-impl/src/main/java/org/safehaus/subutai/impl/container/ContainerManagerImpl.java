package org.safehaus.subutai.impl.container;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.subutai.api.lxcmanager.*;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.impl.strategy.PlacementStrategyFactory;
import org.safehaus.subutai.shared.protocol.Agent;

public class ContainerManagerImpl extends ContainerManagerBase {

    // number sequences for template names used for new clone name generation
    private ConcurrentMap<String, AtomicInteger> sequences;

    public void init() {
        sequences = new ConcurrentHashMap<>();
    }

    public void destroy() {
        sequences.clear();
    }

    @Override
    public Set<Agent> clone(Collection<String> hostNames, String templateName,
            int nodesCount, PlacementStrategyENUM... strategy) {
        LxcPlacementStrategy st = PlacementStrategyFactory.create(nodesCount, strategy);
        try {
            st.calculatePlacement(lxcManager.getPhysicalServerMetrics());
        } catch(LxcCreateException ex) {
            Logger.getLogger(ContainerManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptySet();
        }
        Map<Agent, Integer> slots = st.getPlacementDistribution();

        List<String> cloneNames = new ArrayList<>();
        for(Map.Entry<Agent, Integer> e : slots.entrySet()) {
            for(int i = 0; i < e.getValue(); i++) {
                String name = nextHostName(templateName);
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

    private String nextHostName(String templateName) {
        AtomicInteger i = sequences.putIfAbsent(templateName, new AtomicInteger());
        if(i == null) i = sequences.get(templateName);
        while(true) {
            String name = templateName + "-" + i.incrementAndGet();
            Agent a = agentManager.getAgentByHostname(name);
            if(a == null) return name;
        }
    }

}
