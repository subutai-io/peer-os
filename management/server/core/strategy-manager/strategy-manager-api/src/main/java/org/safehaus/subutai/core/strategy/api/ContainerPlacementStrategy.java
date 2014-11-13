package org.safehaus.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Criteria;


/**
 * Container placement strategy contains methods to distribute containers on physical hosts
 */
public interface ContainerPlacementStrategy
{

    public boolean hasCriteria();

    public String getId();

    public String getTitle();

    public List<CriteriaDef> getCriteriaDef();

    public Map<ServerMetric, Integer> calculateSlots( int nodesCount, List<ServerMetric> serverMetrics );

    /**
     * This method calculates placement of lxcs on physical servers. Code should check passed server metrics to figure
     * out strategy for lxc placement This is done by calling addPlacementInfo method.This method calculates on which
     * physical server to places lxc, the number of lxcs to place and their type
     *
     * @param serverMetrics - map where key is a physical agent and value is a metric
     */
    public void calculatePlacement( int nodesCount, List<ServerMetric> serverMetrics, List<Criteria> criteria )
            throws StrategyException;

    public Map<ServerMetric, Integer> getPlacementDistribution();
}
