package org.safehaus.subutai.core.strategy.api;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * This class should be extended by all container placement strategies
 */
public abstract class AbstractContainerPlacementStrategy implements ContainerPlacementStrategy
{
    private final Map<Agent, Map<String, Integer>> placementInfoMap = new HashMap<>();
    private List<Criteria> criteria = new ArrayList<>();


    public final void addPlacementInfo( Agent physicalNode, String nodeType, int numberOfLxcsToCreate )
    {
        if ( physicalNode == null )
        {
            throw new IllegalArgumentException( "Physical node is null" );
        }
        if ( nodeType == null || nodeType.isEmpty() )
        {
            throw new IllegalArgumentException( "Node type is null or empty" );
        }
        if ( numberOfLxcsToCreate <= 0 )
        {
            throw new IllegalArgumentException( "Number of lxcs must be greater than 0" );
        }

        Map<String, Integer> placementInfo = placementInfoMap.get( physicalNode );
        if ( placementInfo == null )
        {
            placementInfo = new HashMap<>();
            placementInfoMap.put( physicalNode, placementInfo );
        }

        placementInfo.put( nodeType, numberOfLxcsToCreate );
    }


    /**
     * Returns placement map
     *
     * @return map where key is a physical server and value is a map where key is type of node and value is a number of
     * lxcs to place on this server
     */
    public Map<Agent, Map<String, Integer>> getPlacementInfoMap()
    {
        return Collections.unmodifiableMap( placementInfoMap );
    }


    @Override
    public boolean hasCriteria()
    {
        return false;
    }


    @Override
    public List<Criteria> getCriteria()
    {
        return Collections.unmodifiableList( criteria );
    }


    /**
     * Optional method to implement for calculating total number of lxc slots each physical server can accommodate
     *
     * @param serverMetrics - metrics from all connected physical servers
     *
     * @return map where key is a physical agent and value is a number of lxcs this physical server can accommodate
     */
    @Override
    public Map<Agent, Integer> calculateSlots( int nodesCount, Map<Agent, ServerMetric> serverMetrics )
    {
        return null;
    }


    /**
     * Returns a distribution of node counts among severs.
     *
     * @return map where key is a physical server and value is a number of containers to be placed on that server
     */
    @Override
    public Map<Agent, Integer> getPlacementDistribution()
    {
        Map<Agent, Integer> res = new HashMap<>();
        for ( Map.Entry<Agent, Map<String, Integer>> e : placementInfoMap.entrySet() )
        {
            int total = 0;
            for ( Integer i : e.getValue().values() )
            {
                total += i;
            }
            res.put( e.getKey(), total );
        }
        return res;
    }
}
