package io.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.protocol.Criteria;


/**
 * Strategy manager provides methods for working with container placement strategy
 */
public interface StrategyManager
{
    public List<ContainerPlacementStrategy> getPlacementStrategies();

    public Map<ResourceHostMetric, Integer> getPlacementDistribution( List<ResourceHostMetric> serverMetrics,
                                                                      int nodesCount, String strategyId,
                                                                      List<Criteria> criteria )
            throws StrategyException;

    public ContainerPlacementStrategy findStrategyById( String strategyId ) throws StrategyNotFoundException;
}
