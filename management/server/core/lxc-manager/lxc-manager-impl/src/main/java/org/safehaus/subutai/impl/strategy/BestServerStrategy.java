package org.safehaus.subutai.impl.strategy;

import java.util.*;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;

public class BestServerStrategy extends LxcPlacementStrategy {

    public static final String defaultNodeType = "default";

    private final int nodesCount;
    private Set<PlacementStrategyENUM> strategyFactors;

    public BestServerStrategy(int nodesCount, PlacementStrategyENUM... strategyFactors) {
        this.nodesCount = nodesCount;
        this.strategyFactors = EnumSet.noneOf(PlacementStrategyENUM.class);
        this.strategyFactors.addAll(Arrays.asList(strategyFactors));
    }

    @Override
    public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> serverMetrics) {
        Map<Agent, Integer> res = new HashMap<>();
        Map<Agent, Map<String, Integer>> map = getPlacementInfoMap();
        for(Map.Entry<Agent, Map<String, Integer>> e : map.entrySet()) {
            int total = 0;
            for(Integer i : e.getValue().values()) total += i;
            res.put(e.getKey(), total);
        }
        return res;
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {

        // using each startegy criteria, grade servers one by one
        Map<Agent, Integer> grades = new HashMap<>();
        for(Agent a : serverMetrics.keySet()) grades.put(a, 0);
        for(PlacementStrategyENUM sf : strategyFactors) {
            try {
                Agent a = getBestMatch(serverMetrics, MetricComparator.create(sf));
                if(a != null) grades.put(a, grades.get(a) + 1);
            } catch(Exception ex) {
                // comparator not defined for strategy
                // TODO: log
            }
        }

        // sort servers by their grades in decreasing order
        ArrayList<Map.Entry<Agent, Integer>> ls = new ArrayList<>(grades.entrySet());
        Collections.sort(ls, new Comparator<Map.Entry>() {

            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                Integer v1 = (Integer)o1.getValue();
                Integer v2 = (Integer)o2.getValue();
                return -1 * v1.compareTo(v2);
            }
        });

        // distribute nodes count among server with best server first
        Map<Agent, Integer> slots = new HashMap<>();
        for(int i = 0; i < nodesCount; i++) {
            Agent best = ls.get(i % nodesCount).getKey();
            if(slots.containsKey(best)) slots.put(best, slots.get(best) + 1);
            else slots.put(best, 1);
        }
        for(Map.Entry<Agent, Integer> e : slots.entrySet()) {
            addPlacementInfo(e.getKey(), defaultNodeType, e.getValue());
        }
    }

    private Agent getBestMatch(Map<Agent, ServerMetric> serverMetrics, final MetricComparator mc) {

        List<Map.Entry<Agent, ServerMetric>> ls = new ArrayList<>(serverMetrics.entrySet());
        Collections.sort(ls, new Comparator<Map.Entry>() {

            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                int v1 = mc.getValue((ServerMetric)o1.getValue());
                int v2 = mc.getValue((ServerMetric)o2.getValue());
                if(v1 == v2) return 0;
                return v1 < v2 ? -1 : 1;
            }
        });

        int ind = mc.isLessBetter() ? 0 : ls.size() - 1;
        return ls.get(ind).getKey();
    }

}
