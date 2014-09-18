package org.safehaus.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Created by timur on 9/18/14.
 */
public interface StrategyManager {
    public List<ContainerPlacementStrategy> getPlacementStrategies();

    public Map<Agent, Integer> getPlacementDistribution(Map<Agent, ServerMetric> serverMetrics, int nodesCount, String strategyId, List<Criteria> criteria ) throws StrategyException;


    public ContainerPlacementStrategy findStrategyById( String strategyId );
}
