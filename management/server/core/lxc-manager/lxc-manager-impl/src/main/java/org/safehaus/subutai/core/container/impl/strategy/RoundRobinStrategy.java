package org.safehaus.subutai.core.container.impl.strategy;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.core.container.api.lxcmanager.ServerMetric;

import com.google.common.collect.Lists;


public class RoundRobinStrategy extends LxcPlacementStrategy
{

    public static final String DEFAULT_NODE_TYPE = "default";
    private final int nodesCount;


    public RoundRobinStrategy( int nodesCount )
    {
        this.nodesCount = nodesCount;
    }


    public int getNodesCount()
    {
        return nodesCount;
    }


    @Override
    public void calculatePlacement( Map<Agent, ServerMetric> serverMetrics ) throws LxcCreateException
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
        for ( Map.Entry<Agent, Integer> e : slots.entrySet() )
        {
            addPlacementInfo( e.getKey(), DEFAULT_NODE_TYPE, e.getValue() );
        }
    }


    protected List<Agent> sortServers( Map<Agent, ServerMetric> serverMetrics )
    {
        List<Agent> ls = Lists.newArrayList( serverMetrics.keySet() );
        Collections.sort( ls );
        return ls;
    }
}
