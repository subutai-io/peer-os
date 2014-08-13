package org.safehaus.subutai.impl.mongodb;

import java.util.*;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.mongodb.NodeType;
import org.safehaus.subutai.shared.protocol.Agent;

public class CustomPlacementStrategy extends LxcPlacementStrategy {

    private float hddPerNodeMb;
    private float hddReservedMb;
    private float ramPerNodeMb;
    private float ramReservedMb;
    private float cpuPerNodePercentage;
    private float cpuReservedPercentage;

    private final Map<NodeType, Integer> nodesCount;

    public CustomPlacementStrategy(int configServers, int routers, int dataNodes) {
        this.nodesCount = new EnumMap<>(NodeType.class);
        this.nodesCount.put(NodeType.CONFIG_NODE, configServers);
        this.nodesCount.put(NodeType.ROUTER_NODE, routers);
        this.nodesCount.put(NodeType.DATA_NODE, dataNodes);
    }

    public static Map<NodeType, Set<Agent>> getNodes(LxcManager lxcManager,
            int configServers, int routers, int dataNodes) throws LxcCreateException {

        LxcPlacementStrategy strategy = new CustomPlacementStrategy(
                configServers, routers, dataNodes);
        Map<String, Map<Agent, Set<Agent>>> nodes
                = lxcManager.createLxcsByStrategy(strategy);

        // collect nodes by types regardless of parent nodes
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

            int n = Math.round((m.getFreeRamMb() - ramReservedMb) / ramPerNodeMb);
            if((min = Math.min(n, min)) <= 0) continue;

            n = Math.round((m.getFreeHddMb() - hddReservedMb) / hddPerNodeMb);
            if((min = Math.min(n, min)) <= 0) continue;

            // TODO: check cpu load when cpu load determination is reimplemented

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

    public void setCriteria(NodeType type) {
        switch(type) {
            case CONFIG_NODE:
                hddPerNodeMb = GB2MB(5);
                hddReservedMb = GB2MB(10);
                ramPerNodeMb = GB2MB(1);
                ramReservedMb = GB2MB(1);
                cpuPerNodePercentage = 5;
                cpuReservedPercentage = 10;
                break;
            case ROUTER_NODE:
                hddPerNodeMb = GB2MB(3);
                hddReservedMb = GB2MB(5);
                ramPerNodeMb = GB2MB(0.5f);
                ramReservedMb = GB2MB(1);
                cpuPerNodePercentage = 5;
                cpuReservedPercentage = 10;
                break;
            case DATA_NODE:
                hddPerNodeMb = GB2MB(20);
                hddReservedMb = GB2MB(30);
                ramPerNodeMb = GB2MB(1);
                ramReservedMb = GB2MB(1);
                cpuPerNodePercentage = 5;
                cpuReservedPercentage = 10;
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
            int cnt = 1;
            if(info != null && info.get(type.toString()) != null)
                cnt = info.get(type.toString()).intValue() + 1;
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
