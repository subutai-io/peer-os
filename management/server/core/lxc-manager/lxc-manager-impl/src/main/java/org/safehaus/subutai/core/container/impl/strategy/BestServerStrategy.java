package org.safehaus.subutai.core.container.impl.strategy;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.container.api.lxcmanager.ServerMetric;


public class BestServerStrategy extends RoundRobinStrategy
{

    private Set<PlacementStrategy> strategyFactors;


    public BestServerStrategy( int nodesCount, PlacementStrategy... strategyFactors )
    {
        super( nodesCount );
        this.strategyFactors = EnumSet.noneOf( PlacementStrategy.class );
        this.strategyFactors.addAll( Arrays.asList( strategyFactors ) );
    }


    @Override
    protected List<Agent> sortServers( Map<Agent, ServerMetric> serverMetrics )
    {
        // using each startegy criteria, grade servers one by one
        Map<Agent, Integer> grades = new HashMap<>();
        for ( Agent a : serverMetrics.keySet() )
        {
            grades.put( a, 0 );
        }
        for ( PlacementStrategy sf : strategyFactors )
        {
            try
            {
                Agent a = getBestMatch( serverMetrics, MetricComparator.create( sf ) );
                if ( a != null )
                {
                    grades.put( a, grades.get( a ) + 1 );
                }
            }
            catch ( Exception ex )
            {
                // comparator not defined for strategy
                // TODO: log
            }
        }

        // sort servers by their grades in decreasing order
        ArrayList<Map.Entry<Agent, Integer>> ls = new ArrayList<>( grades.entrySet() );
        Collections.sort( ls, new Comparator<Map.Entry>()
        {

            @Override
            public int compare( Map.Entry o1, Map.Entry o2 )
            {
                Integer v1 = ( Integer ) o1.getValue();
                Integer v2 = ( Integer ) o2.getValue();
                return -1 * v1.compareTo( v2 );
            }
        } );

        List<Agent> servers = new ArrayList<>();
        for ( Map.Entry<Agent, Integer> e : ls )
        {
            servers.add( e.getKey() );
        }
        return servers;
    }


    private Agent getBestMatch( Map<Agent, ServerMetric> serverMetrics, final MetricComparator mc )
    {

        List<Map.Entry<Agent, ServerMetric>> ls = new ArrayList<>( serverMetrics.entrySet() );
        Collections.sort( ls, new Comparator<Map.Entry>()
        {

            @Override
            public int compare( Map.Entry o1, Map.Entry o2 )
            {
                int v1 = mc.getValue( ( ServerMetric ) o1.getValue() );
                int v2 = mc.getValue( ( ServerMetric ) o2.getValue() );
                return Integer.compare( v1, v2 );
            }
        } );

        int ind = mc.isLessBetter() ? 0 : ls.size() - 1;
        return ls.get( ind ).getKey();
    }
}
