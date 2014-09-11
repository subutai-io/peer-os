package org.safehaus.subutai.impl.strategymanager;

import org.safehaus.subutai.api.strategymanager.ContainerPlacementStrategy;
import org.safehaus.subutai.api.strategymanager.Criteria;
import org.safehaus.subutai.api.strategymanager.PlacementStrategyFactory;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.List;


public class PlacementStrategyFactoryImpl implements PlacementStrategyFactory {

	public PlacementStrategy getDefaultStrategyType() {
		return PlacementStrategy.ROUND_ROBIN;
	}

    @Override
    public ContainerPlacementStrategy create(int nodesCount, PlacementStrategy strategy, List<Criteria> criteria) {
        if (PlacementStrategy.ROUND_ROBIN.equals(strategy))
            return new RoundRobinStrategy(nodesCount);
        if (PlacementStrategy.FILLUP_PROCEED.equals(strategy))
            return new DefaultContainerPlacementStrategy(nodesCount);
        if (PlacementStrategy.BEST_SERVER.equals(strategy))
            return new BestServerStrategy(nodesCount, criteria);
        return getDefaultStrategy(nodesCount, criteria);
    }

    @Override
    public ContainerPlacementStrategy getDefaultStrategy(int nodesCount, List<Criteria> criteria) {
        return new RoundRobinStrategy(nodesCount);
    }

}
