package org.safehaus.subutai.impl.containermanager.strategy;

import org.safehaus.subutai.api.containermanager.ContainerPlacementStrategy;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.impl.containermanager.strategy.BestServerStrategy;
import org.safehaus.subutai.impl.containermanager.strategy.DefaultContainerPlacementStrategy;
import org.safehaus.subutai.impl.containermanager.strategy.RoundRobinStrategy;

import java.util.EnumSet;
import java.util.Set;


public class PlacementStrategyFactory {

	public static ContainerPlacementStrategy create(int nodesCount, PlacementStrategy... strategy) {
		// round-robin is the default
		RoundRobinStrategy rr = new RoundRobinStrategy(nodesCount);
		if (strategy == null || strategy.length == 0) return rr;

		Set<PlacementStrategy> set = EnumSet.of(strategy[0], strategy);
		if (set.contains(PlacementStrategy.ROUND_ROBIN)) return rr;
		if (set.contains(PlacementStrategy.FILLUP_PROCEED))
			return new DefaultContainerPlacementStrategy(nodesCount);

		return new BestServerStrategy(nodesCount, strategy);
	}

	public static PlacementStrategy getDefaultStrategyType() {
		return PlacementStrategy.ROUND_ROBIN;
	}

}
