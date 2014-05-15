package org.safehaus.kiskis.mgmt.impl.mongodb;

import java.util.*;
import org.safehaus.kiskis.mgmt.api.lxcmanager.*;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class CustomPlacementStrategy extends LxcPlacementStrategy {

    private float hdd_per_node_mb;
    private float hdd_in_reserve_mb;
    private float ram_per_node_mb;
    private float ram_in_reserve_mb;
    private float cpu_per_node_percentage;
    private float cpu_in_reserve_percentage;

    private final Map<NodeType, Integer> nodesCount;

    public CustomPlacementStrategy(int configServersCount, int routersCount, int dataNodesCount) {
        this.nodesCount = new EnumMap<>(NodeType.class);
        this.nodesCount.put(NodeType.CONFIG_NODE, configServersCount);
        this.nodesCount.put(NodeType.ROUTER_NODE, routersCount);
        this.nodesCount.put(NodeType.DATA_NODE, dataNodesCount);
    }

    public static Map<NodeType, Set<Agent>> getNodes(LxcManager lxcManager,
            int configServersCount, int routersCount, int dataNodesCount) throws LxcCreateException {

        LxcPlacementStrategy strategy = new CustomPlacementStrategy(
                configServersCount, routersCount, dataNodesCount);
        Map<String, Map<Agent, Set<Agent>>> nodes
                = lxcManager.createLxcsByStrategy(strategy);

        Map<NodeType, Set<Agent>> res = new EnumMap<>(NodeType.class);
        for(NodeType type : NodeType.values()) {
            Map<Agent, Set<Agent>> map = nodes.get(type.toString());
            if(map == null) continue;

            Set<Agent> all = new HashSet<>();
            for(Set<Agent> children : map.values()) all.addAll(children);

            Set<Agent> set = res.get(type);
            if(set != null) set.addAll(all);
            else res.put(type, all);
        }
        return res;
    }

    @Override
    public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> metrics) {
        if(metrics == null || metrics.isEmpty()) return null;

        Map<Agent, Integer> slots = new HashMap<>();
        for(Map.Entry<Agent, ServerMetric> e : metrics.entrySet()) {
            ServerMetric m = e.getValue();
            int min = Integer.MAX_VALUE;

            int n = Math.round((m.getFreeRamMb() - ram_in_reserve_mb) / ram_per_node_mb);
            if((min = Math.min(n, min)) <= 0) continue;

            n = Math.round((m.getFreeHddMb() - hdd_in_reserve_mb) / hdd_per_node_mb);
            if((min = Math.min(n, min)) <= 0) continue;

            int unusedCpu = 100 - m.getCpuLoadPercent();
            n = Math.round(unusedCpu - cpu_in_reserve_percentage / cpu_per_node_percentage);
            if((min = Math.min(n, min)) <= 0) continue;

            slots.put(e.getKey(), min);
        }
        return slots;
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {
        for(NodeType type : NodeType.values()) {
            setCriteria(type);

            Map<Agent, Integer> serverSlots = calculateSlots(serverMetrics);
            if(serverSlots == null || serverSlots.isEmpty()) return;

            int available = 0;
            for(Integer i : serverSlots.values()) available += i.intValue();
            if(available < nodesCount.get(type)) return;

            calculatePlacement(type, serverSlots);
        }
    }

    private void setCriteria(NodeType type) {
        switch(type) {
            case CONFIG_NODE:
                hdd_per_node_mb = GB2MB(3);
                hdd_in_reserve_mb = GB2MB(5);
                ram_per_node_mb = GB2MB(0.5f);
                ram_in_reserve_mb = GB2MB(1);
                cpu_per_node_percentage = 5;
                cpu_in_reserve_percentage = 10;
                break;
            case ROUTER_NODE:
                hdd_per_node_mb = GB2MB(3);
                hdd_in_reserve_mb = GB2MB(5);
                ram_per_node_mb = GB2MB(0.5f);
                ram_in_reserve_mb = GB2MB(1);
                cpu_per_node_percentage = 5;
                cpu_in_reserve_percentage = 10;
                break;
            case DATA_NODE:
                hdd_per_node_mb = GB2MB(100);
                hdd_in_reserve_mb = GB2MB(10);
                ram_per_node_mb = GB2MB(1);
                ram_in_reserve_mb = GB2MB(1);
                cpu_per_node_percentage = 5;
                cpu_in_reserve_percentage = 10;
                break;
            default:
                throw new AssertionError(type.name());

        }
    }

    private void calculatePlacement(NodeType type, Map<Agent, Integer> serverSlots) throws LxcCreateException {
        for(int i = 0; i < nodesCount.get(type); i++) {
            Agent physicalNode = findBestServer(serverSlots);
            if(physicalNode == null) break;

            Integer slotsCount = serverSlots.get(physicalNode);
            serverSlots.put(physicalNode, slotsCount - 1);

            Map<String, Integer> info = getPlacementInfoMap().get(physicalNode);
            int cnt = info != null ? info.get(type.toString()) + 1 : 1;
            addPlacementInfo(physicalNode, type.toString(), cnt);
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

    private int GB2MB(float gb) {
        return Math.round(gb * 1024);
    }

}
