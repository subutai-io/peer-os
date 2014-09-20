package org.safehaus.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Created by timur on 9/11/14.
 */
public interface ContainerPlacementStrategy
{

    public boolean hasCriteria();

    public String getId();

    public String getTitle();

    public List<Criteria> getCriteria();

    public Map<Agent, Integer> calculateSlots( int nodesCount, Map<Agent, ServerMetric> serverMetrics );

    /**
     * This method calculates placement of lxcs on physical servers. Code should check passed server metrics to figure
     * out strategy for lxc placement This is done by calling addPlacementInfo method.This method calculates on which
     * physical server to places lxc, the number of lxcs to place and their type
     *
     * @param serverMetrics - map where key is a physical agent and value is a metric
     */
    public void calculatePlacement( int nodesCount, Map<Agent, ServerMetric> serverMetrics, List<Criteria> criteria );

    public Map<Agent, Integer> getPlacementDistribution();
}
