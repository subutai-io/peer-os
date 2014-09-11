package org.safehaus.subutai.impl.strategymanager;

import org.safehaus.subutai.api.strategymanager.AbstractContainerPlacementStrategy;
import org.safehaus.subutai.api.strategymanager.ServerMetric;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.*;

public class RoundRobinStrategy extends AbstractContainerPlacementStrategy {

	public static final String DEFAULT_NODE_TYPE = "default";

    public RoundRobinStrategy(int nodesCount) {
            super(nodesCount);
    }

	@Override
	public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) {
		if (serverMetrics == null || serverMetrics.isEmpty()) return;

		List<Agent> ls = sortServers(serverMetrics);

		// distribute required nodes among servers in round-robin fashion
		Map<Agent, Integer> slots = new HashMap<Agent, Integer>();
		for (int i = 0; i < getNodesCount(); i++) {
			Agent best = ls.get(i % ls.size());
			if (slots.containsKey(best)) slots.put(best, slots.get(best) + 1);
			else slots.put(best, 1);
		}
		// add node distribution counts
		for (Map.Entry<Agent, Integer> e : slots.entrySet()) {
			addPlacementInfo(e.getKey(), DEFAULT_NODE_TYPE, e.getValue());
		}
	}

    @Override
    public PlacementStrategy getStrategy() {
        return PlacementStrategy.ROUND_ROBIN;
    }

    protected List<Agent> sortServers(Map<Agent, ServerMetric> serverMetrics) {
		List<Agent> ls = new ArrayList(serverMetrics.keySet());
		Collections.sort(ls);
		return ls;
	}

}
