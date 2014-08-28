package org.safehaus.subutai.impl.strategy;

import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;

import java.util.EnumSet;
import java.util.Set;


public class PlacementStrategyFactory {

	public static LxcPlacementStrategy create(int nodesCount, PlacementStrategy... strategy) {
		// round-robin is the default
		RoundRobinStrategy rr = new RoundRobinStrategy(nodesCount);
		if (strategy == null || strategy.length == 0) return rr;

		Set<PlacementStrategy> set = EnumSet.of(strategy[0], strategy);
		if (set.contains(PlacementStrategy.ROUND_ROBIN)) return rr;
		if (set.contains(PlacementStrategy.FILLUP_PROCEED))
			return new DefaultLxcPlacementStrategy(nodesCount);

		return new BestServerStrategy(nodesCount, strategy);
	}

	public static PlacementStrategy getDefaultStrategyType() {
		return PlacementStrategy.ROUND_ROBIN;
	}

}
