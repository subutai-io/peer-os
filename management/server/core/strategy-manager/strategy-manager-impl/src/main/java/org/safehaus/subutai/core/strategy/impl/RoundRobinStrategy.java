package org.safehaus.subutai.core.strategy.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.strategy.api.AbstractContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;

import com.google.common.collect.Lists;


public class RoundRobinStrategy extends AbstractContainerPlacementStrategy
{

    public static final String DEFAULT_NODE_TYPE = "default";


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
    public void calculatePlacement( int nodesCount, Map<Agent, ServerMetric> serverMetrics, List<Criteria> criteria )
            throws StrategyException
    {
        if ( serverMetrics == null || serverMetrics.isEmpty() )
        {
            return;
        }

        List<Agent> ls = sortServers( serverMetrics );

        // distribute required nodes among servers in round-robin fashion
        Map<Agent, Integer> slots = new HashMap<>();
        for ( int i = 0; i < nodesCount; i++ )
        {
            Agent best = ls.get( i % ls.size() );
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
        clearPlacementInfo();
        for ( Map.Entry<Agent, Integer> e : slots.entrySet() )
        {
            addPlacementInfo( e.getKey(), DEFAULT_NODE_TYPE, e.getValue() );
        }
    }


    protected List<Agent> sortServers( Map<Agent, ServerMetric> serverMetrics ) throws StrategyException
    {
        List<Agent> ls = Lists.newArrayList( serverMetrics.keySet() );
        Collections.sort( ls );
        return ls;
    }
}
