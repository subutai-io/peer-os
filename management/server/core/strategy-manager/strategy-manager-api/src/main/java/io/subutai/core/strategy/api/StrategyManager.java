package io.subutai.core.strategy.api;


import java.util.List;


/**
 * Strategy manager provides methods for working with container placement strategy
 */
public interface StrategyManager
{
    List<ContainerPlacementStrategy> getPlacementStrategies();

//    Map<ResourceHostMetric, Integer> getPlacementDistribution( ResourceHostMetrics serverMetrics,
//                                                                      int nodesCount, String strategyId,
//                                                                      List<Criteria> criteria )
//            throws StrategyException;

    ContainerPlacementStrategy findStrategyById( String strategyId ) throws StrategyNotFoundException;

    List<String> getPlacementStrategyTitles();
}
