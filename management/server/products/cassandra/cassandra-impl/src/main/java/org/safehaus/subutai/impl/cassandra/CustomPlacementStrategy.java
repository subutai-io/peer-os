package org.safehaus.subutai.impl.cassandra;

import java.util.*;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.shared.protocol.Agent;

class CustomPlacementStrategy extends LxcPlacementStrategy {

    public static final String DEFAULT_NODE_TYPE = "default";
    private final float HDD_PER_NODE_MB = 1024 * 20;
    private final float HDD_IN_RESERVE_MB = 1024 * 10;
    private final float RAM_PER_NODE_MB = 1024 * 2;
    private final float RAM_IN_RESERVE_MB = 1024;
    private final float CPU_PER_NODE_PERCENTAGE = 20;
    private final float CPU_IN_RESERVE_PERCENTAGE = 10;

    private final int nodesCount;

    public CustomPlacementStrategy(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    public static Map<Agent, Set<Agent>> createNodes(LxcManager lxcManager, int nodesCount) throws LxcCreateException {
        Map<String, Map<Agent, Set<Agent>>> map = lxcManager.createLxcsByStrategy(
                new CustomPlacementStrategy(nodesCount));

        // ignore node types and group nodes by physical servers
        Map<Agent, Set<Agent>> res = new HashMap<>();
        for(Map<Agent, Set<Agent>> m : map.values()) {
            for(Map.Entry<Agent, Set<Agent>> e : m.entrySet()) {
                Set<Agent> nodes = res.get(e.getKey());
                if(nodes != null) nodes.addAll(e.getValue());
                else res.put(e.getKey(), e.getValue());
            }
        }
        return res;
    }

    @Override
    public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> metrics) {
        Map<Agent, Integer> slots = new HashMap<>();
        if(metrics == null || metrics.isEmpty()) return slots;

        for(Map.Entry<Agent, ServerMetric> e : metrics.entrySet()) {
            ServerMetric m = e.getValue();
            int min = Integer.MAX_VALUE;

            int n = Math.round((m.getFreeRamMb() - RAM_IN_RESERVE_MB) / RAM_PER_NODE_MB);
            if((min = Math.min(n, min)) <= 0) continue;

            n = Math.round((m.getFreeHddMb() - HDD_IN_RESERVE_MB) / HDD_PER_NODE_MB);
            if((min = Math.min(n, min)) <= 0) continue;

            // TODO: check cpu load when cpu load determination is reimplemented

            slots.put(e.getKey(), min);
        }
        return slots;
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {

        Map<Agent, Integer> serversSlots = calculateSlots(serverMetrics);
        if(serversSlots.isEmpty()) return;

        int available = 0;
        for(Integer i : serversSlots.values()) available += i.intValue();
        if(available < nodesCount) return;

        for(int i = 0; i < nodesCount; i++) {
            Agent physicalNode = findBestServer(serversSlots);
            if(physicalNode == null) break;

            Integer slotsCount = serversSlots.get(physicalNode);
            serversSlots.put(physicalNode, slotsCount - 1);

            Map<String, Integer> info = getPlacementInfoMap().get(physicalNode);
            int cnt = info != null ? info.get(DEFAULT_NODE_TYPE) + 1 : 1;
            addPlacementInfo(physicalNode, DEFAULT_NODE_TYPE, cnt);
        }

    }

    private Agent findBestServer(Map<Agent, Integer> map) {
        int max = 0;
        Agent best = null;
        for(Map.Entry<Agent, Integer> e : map.entrySet()) {
            if(e.getValue().intValue() > max) {
                best = e.getKey();
                max = e.getValue().intValue();
            }
        }
        return best;
    }

}
