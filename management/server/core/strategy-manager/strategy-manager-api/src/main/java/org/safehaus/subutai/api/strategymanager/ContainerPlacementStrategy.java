package org.safehaus.subutai.api.strategymanager;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.Map;

/**
 * Created by timur on 9/11/14.
 */
public interface ContainerPlacementStrategy {
    public int getNodesCount();

    public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> serverMetrics);

    /**
     * This method calculates placement of lxcs on physical servers. Code should
     * check passed server metrics to figure out strategy for lxc placement This
     * is done by calling addPlacementInfo method.This method calculates on
     * which physical server to places lxc, the number of lxcs to place and
     * their type
     *
     * @param serverMetrics - map where key is a physical agent and value is a
     *                      metric
     */
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics);

    public Map<Agent, Integer> getPlacementDistribution();

    public PlacementStrategy getStrategy();
}
