package org.safehaus.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Strategy manager provides methods for working with container placement strategy
 */
public interface StrategyManager
{
    public List<ContainerPlacementStrategy> getPlacementStrategies();

    public Map<Agent, Integer> getPlacementDistribution( Map<Agent, ServerMetric> serverMetrics, int nodesCount,
                                                         String strategyId, List<Criteria> criteria )
            throws StrategyException;


    public ContainerPlacementStrategy findStrategyById( String strategyId ) throws StrategyNotAvailable;
}
