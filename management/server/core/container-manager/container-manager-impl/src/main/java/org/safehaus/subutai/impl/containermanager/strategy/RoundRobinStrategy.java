package org.safehaus.subutai.impl.containermanager.strategy;

import org.safehaus.subutai.api.containermanager.ContainerCreateException;
import org.safehaus.subutai.api.containermanager.ContainerPlacementStrategy;
import org.safehaus.subutai.api.containermanager.ServerMetric;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.*;

public class RoundRobinStrategy extends ContainerPlacementStrategy {

	public static final String DEFAULT_NODE_TYPE = "default";
	private final int nodesCount;

	public RoundRobinStrategy(int nodesCount) {
		this.nodesCount = nodesCount;
	}

	public int getNodesCount() {
		return nodesCount;
	}

	@Override
	public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws ContainerCreateException {
		if (serverMetrics == null || serverMetrics.isEmpty()) return;

		List<Agent> ls = sortServers(serverMetrics);

		// distribute required nodes among servers in round-robin fashion
		Map<Agent, Integer> slots = new HashMap<>();
		for (int i = 0; i < nodesCount; i++) {
			Agent best = ls.get(i % ls.size());
			if (slots.containsKey(best)) slots.put(best, slots.get(best) + 1);
			else slots.put(best, 1);
		}
		// add node distribution counts
		for (Map.Entry<Agent, Integer> e : slots.entrySet()) {
			addPlacementInfo(e.getKey(), DEFAULT_NODE_TYPE, e.getValue());
		}
	}

	protected List<Agent> sortServers(Map<Agent, ServerMetric> serverMetrics) {
		List<Agent> ls = new ArrayList(serverMetrics.keySet());
		Collections.sort(ls);
		return ls;
	}

}
