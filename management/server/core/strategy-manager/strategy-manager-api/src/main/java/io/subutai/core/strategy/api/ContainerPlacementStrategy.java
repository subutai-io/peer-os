package io.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.protocol.Criteria;


/**
 * Container placement strategy contains methods to distribute containers across resource hosts
 */
public interface ContainerPlacementStrategy
{

    public boolean hasCriteria();

    public String getId();

    public String getTitle();

    public List<CriteriaDef> getCriteriaDef();

    public Map<ResourceHostMetric, Integer> calculateSlots( int nodesCount, List<ResourceHostMetric> serverMetrics );

    /**
     * This method calculates placement of containers across physical servers. Code should check passed server metrics
     * to figure out strategy for container placement This is done by calling addPlacementInfo method.This method
     * calculates on which resource host to place containers, the number of containers to place and their type
     */
    public void calculatePlacement( int nodesCount, List<ResourceHostMetric> serverMetrics, List<Criteria> criteria )
            throws StrategyException;

    public Map<ResourceHostMetric, Integer> getPlacementDistribution();
}
