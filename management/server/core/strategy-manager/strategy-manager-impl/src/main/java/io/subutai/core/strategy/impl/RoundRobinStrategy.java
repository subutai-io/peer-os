package io.subutai.core.strategy.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.util.CollectionUtil;
import io.subutai.core.strategy.api.AbstractContainerPlacementStrategy;
import io.subutai.core.strategy.api.StrategyException;


public class RoundRobinStrategy extends AbstractContainerPlacementStrategy
{


    @Override
    public String getId()
    {
        return "ROUND_ROBIN";
    }


    @Override
    public String getTitle()
    {
        return "Round Robin placement strategy";
    }


    @Override
    public void calculatePlacement( int nodesCount, List<ResourceHostMetric> serverMetrics, List<Criteria> criteria )
            throws StrategyException
    {
        if ( CollectionUtil.isCollectionEmpty( serverMetrics ) )
        {
            return;
        }

        setDistributionCriteria( criteria );

        List<ResourceHostMetric> sortedMetrics = sortServers( serverMetrics );


        // distribute required nodes among servers in round-robin fashion
        Map<ResourceHostMetric, Integer> slots = new HashMap<>();
        for ( int i = 0; i < nodesCount; i++ )
        {
            ResourceHostMetric best = sortedMetrics.get( i % sortedMetrics.size() );
            if ( slots.containsKey( best ) )
            {
                slots.put( best, slots.get( best ) + 1 );
            }
            else
            {
                slots.put( best, 1 );
            }
        }
        // add node distribution counts
        for ( Map.Entry<ResourceHostMetric, Integer> e : slots.entrySet() )
        {
            addPlacementInfo( e.getKey(), DEFAULT_NODE_TYPE, e.getValue() );
        }
    }
}
