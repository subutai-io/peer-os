package org.safehaus.subutai.impl.strategymanager;

import org.safehaus.subutai.api.strategymanager.AbstractContainerPlacementStrategy;
import org.safehaus.subutai.api.strategymanager.Criteria;
import org.safehaus.subutai.api.strategymanager.PlacementStrategyFactory;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.List;


public class PlacementStrategyFactoryImpl implements PlacementStrategyFactory {

	public PlacementStrategy getDefaultStrategyType() {
		return PlacementStrategy.ROUND_ROBIN;
	}

    @Override
    public AbstractContainerPlacementStrategy create(int nodesCount, PlacementStrategy strategy, List<Criteria> criteria) {
        if (PlacementStrategy.ROUND_ROBIN.equals(strategy))
            return new RoundRobinStrategy();
        if (PlacementStrategy.FILLUP_PROCEED.equals(strategy))
            return new DefaultContainerPlacementStrategy();
        if (PlacementStrategy.BEST_SERVER.equals(strategy))
            return new BestServerStrategy();
        return getDefaultStrategy(nodesCount, criteria);
    }

    @Override
    public AbstractContainerPlacementStrategy getDefaultStrategy(int nodesCount, List<Criteria> criteria) {
        return new RoundRobinStrategy();
    }

}
