package org.safehaus.subutai.api.strategymanager;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.*;

/**
 * This class should be extended by all container placement strategies
 */
public abstract class ContainerPlacementStrategy {
    private final Map<Agent, Map<String, Integer>> placementInfoMap = new HashMap<Agent, Map<String, Integer>>();
    private List<Criteria> criteria = new ArrayList<Criteria>();

    private int nodesCount;

    public ContainerPlacementStrategy(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    public ContainerPlacementStrategy(int nodesCount, List<Criteria> criteria) {
        this.criteria = criteria;
    }

    /**
     * Optional method to implement for calculating total number of lxc slots
     * each physical server can accommodate
     *
     * @param serverMetrics - metrics from all connected physical servers
     * @return map where key is a physical agent and value is a number of lxcs
     * this physical server can accommodate
     */
    public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> serverMetrics) {
        return null;
    }

    public final void addPlacementInfo(Agent physicalNode, String nodeType, int numberOfLxcsToCreate) {
        if (physicalNode == null)
            throw new IllegalArgumentException("Physical node is null");
        if (nodeType == null || nodeType.isEmpty())
            throw new IllegalArgumentException("Node type is null or empty");
        if (numberOfLxcsToCreate <= 0)
            throw new IllegalArgumentException("Number of lxcs must be greater than 0");

        Map<String, Integer> placementInfo = placementInfoMap.get(physicalNode);
        if (placementInfo == null) {
            placementInfo = new HashMap<String, Integer>();
            placementInfoMap.put(physicalNode, placementInfo);
        }

        placementInfo.put(nodeType, numberOfLxcsToCreate);
    }

    public void addCriteria(Criteria criteria) {
        if (criteria == null)
            throw new IllegalArgumentException("Criteria could not be null.");
        this.criteria.add(criteria);
    }

    public List<Criteria> getCriteria() {
        return Collections.unmodifiableList(criteria);
    }

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
    public abstract void calculatePlacement(Map<Agent, ServerMetric> serverMetrics);

    /**
     * Returns placement map
     *
     * @return map where key is a physical server and value is a map where key
     * is type of node and value is a number of lxcs to place on this server
     */
    public Map<Agent, Map<String, Integer>> getPlacementInfoMap() {
        return Collections.unmodifiableMap(placementInfoMap);
    }

    /**
     * Returns a distribution of node counts among severs.
     *
     * @return map where key is a physical server and value is a number of
     * containers to be placed on that server
     */
    public Map<Agent, Integer> getPlacementDistribution() {
        Map<Agent, Integer> res = new HashMap<Agent, Integer>();
        for (Map.Entry<Agent, Map<String, Integer>> e : placementInfoMap.entrySet()) {
            int total = 0;
            for (Integer i : e.getValue().values()) total += i;
            res.put(e.getKey(), total);
        }
        return res;
    }

    public abstract PlacementStrategy getStrategy();
}
