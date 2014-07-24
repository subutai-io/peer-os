package org.safehaus.subutai.impl.hadoop.operation;

import java.util.*;

import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.shared.protocol.Agent;

class CustomPlacementStrategy extends LxcPlacementStrategy {

    public static final String MASTER_NODE_TYPE = "master";
    public static final String SLAVE_NODE_TYPE = "slave";
    private float hddPerNodeMb;
    private float hddReservedMb;
    private float ramPerNodeMb;
    private float ramReservedMb;
    private float cpuPerNodePercentage;
    private float cpuReservedPercentage;

    private final Map<String, Integer> nodesCount;


    public CustomPlacementStrategy(int masterNodes, int slaveNodes) {
        this.nodesCount = new HashMap<>();
        this.nodesCount.put(MASTER_NODE_TYPE, masterNodes);
        this.nodesCount.put(SLAVE_NODE_TYPE, slaveNodes);
    }


    private static Map<String, Set<Agent>> getFromExistingAgents( LxcManager lxcManager ) throws LxcCreateException {

        Set<Agent> agents = lxcManager.getAgentManager().getAgents();

        if ( agents.isEmpty() || agents.size() < 5 ) {
            throw new LxcCreateException( "Not enough agents" );
        }

        HashSet<Agent> masterNodeAgents = new HashSet<>();
        HashSet<Agent> slaveNodeAgents = new HashSet<>();
        int i = 1;

        for ( Agent agent : agents ) {
            if ( i <= 3 ) {
                masterNodeAgents.add( agent );
            } else {
                slaveNodeAgents.add( agent );
            }

            i++;
        }

        Map<String, Set<Agent>> agentMap = new HashMap<>();
        agentMap.put( MASTER_NODE_TYPE, masterNodeAgents );
        agentMap.put( SLAVE_NODE_TYPE, slaveNodeAgents );

        return agentMap;
    }


    public static Map<String, Set<Agent>> getNodes( LxcManager lxcManager, int masterNodes, int slaveNodes )
            throws LxcCreateException {

        // Dump method used for testing
//        if ( true ) {
//            return getFromExistingAgents( lxcManager );
//        }

        LxcPlacementStrategy strategy = new CustomPlacementStrategy( masterNodes, slaveNodes );
        Map<String, Map<Agent, Set<Agent>>> nodes = lxcManager.createLxcsByStrategy( strategy );

        // Collect nodes by types regardless of parent nodes
        Map<String, Set<Agent>> res = new HashMap<>();

        for ( String type : new String[] { MASTER_NODE_TYPE, SLAVE_NODE_TYPE } ) {
            Map<Agent, Set<Agent>> map = nodes.get( type );
            if ( map == null ) {
                throw new LxcCreateException( "No nodes for type " + type );
            }

            Set<Agent> all = new HashSet<>();
            for ( Set<Agent> children : map.values() ) {
                all.addAll( children );
            }

            Set<Agent> set = res.get( type );
            if ( set != null ) {
                set.addAll( all );
            }
            else {
                res.put( type, all );
            }
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

            int unusedCpu = 100 - m.getCpuLoadPercent();
            n = Math.round(unusedCpu - cpuReservedPercentage / cpuPerNodePercentage);
            if((min = Math.min(n, min)) <= 0) continue;

            slots.put(e.getKey(), min);
        }
        return slots;
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {
        for(String type : new String[]{MASTER_NODE_TYPE, SLAVE_NODE_TYPE}) {

            setCriteria(type);
            Map<Agent, Integer> serverSlots = calculateSlots(serverMetrics);
            if(serverSlots == null || serverSlots.isEmpty()) return;

            int available = 0;
            for(Integer i : serverSlots.values()) available += i.intValue();
            if(available < nodesCount.get(type)) return;

            calculatePlacement(type, serverSlots);
        }
    }

    public void setCriteria(String type) {
        switch(type) {
            case MASTER_NODE_TYPE:
                hddPerNodeMb = GB2MB(10);
                hddReservedMb = GB2MB(50);
                ramPerNodeMb = GB2MB(1);
                ramReservedMb = GB2MB(2);
                cpuPerNodePercentage = 10;
                cpuReservedPercentage = 20;
                break;
            case SLAVE_NODE_TYPE:
                hddPerNodeMb = GB2MB(30);
                hddReservedMb = GB2MB(50);
                ramPerNodeMb = GB2MB(1);
                ramReservedMb = GB2MB(1);
                cpuPerNodePercentage = 5;
                cpuReservedPercentage = 10;
                break;
            default:
                throw new AssertionError("Invalid node type");

        }
    }

    private void calculatePlacement(String type, Map<Agent, Integer> serverSlots) throws LxcCreateException {
        for(int i = 0; i < nodesCount.get(type); i++) {
            Agent physicalNode = findBestServer(serverSlots);
            if(physicalNode == null) break;

            Integer slotsCount = serverSlots.get(physicalNode);
            serverSlots.put(physicalNode, slotsCount - 1);

            Map<String, Integer> info = getPlacementInfoMap().get(physicalNode);
            int cnt = 1;
            if(info != null && info.get(type) != null)
                cnt = info.get(type).intValue() + 1;
            addPlacementInfo(physicalNode, type, cnt);
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
