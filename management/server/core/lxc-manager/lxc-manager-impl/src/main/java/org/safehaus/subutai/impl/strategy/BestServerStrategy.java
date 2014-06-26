package org.safehaus.subutai.impl.strategy;

import java.util.*;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;

public class BestServerStrategy extends LxcPlacementStrategy {

    public static final String defaultNodeType = "default";

    private Set<PlacementStrategyENUM> strategyFactors;

    public BestServerStrategy(PlacementStrategyENUM... strategyFactors) {
        this.strategyFactors = EnumSet.noneOf(PlacementStrategyENUM.class);
        this.strategyFactors.addAll(Arrays.asList(strategyFactors));
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {

        // using each startegy criteria, grade servers one by one
        Map<Agent, Integer> grades = new HashMap<>();
        for(PlacementStrategyENUM sf : strategyFactors) {
            try {
                Agent a = getBestMatch(serverMetrics, MetricComparator.create(sf));
                if(a != null) {
                    Integer g = grades.get(a);
                    grades.put(a, (g != null ? g : 0) + 1);
                }
            } catch(Exception ex) {
                // comparator not defined for strategy
                // TODO: log
            }
        }

        // sort servers by their grades
        ArrayList<Map.Entry<Agent, Integer>> ls = new ArrayList<>(grades.entrySet());
        Collections.sort(ls, new Comparator<Map.Entry>() {

            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                Integer v1 = (Integer)o1.getValue();
                Integer v2 = (Integer)o2.getValue();
                return v1.compareTo(v2);
            }
        });

        Agent best = ls.get(ls.size() - 1).getKey();
        addPlacementInfo(best, defaultNodeType, 1);
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
