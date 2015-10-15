package io.subutai.core.strategy.impl;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.protocol.Criteria;
import io.subutai.core.strategy.api.CriteriaDef;
import io.subutai.core.strategy.api.StrategyException;

import com.google.common.collect.Lists;


public class BestServerStrategy extends RoundRobinStrategy
{

    @Override
    public String getId()
    {
        return "BEST_SERVER";
    }


    @Override
    public String getTitle()
    {
        return "Best server placement strategy";
    }


    @Override
    protected List<ResourceHostMetric> sortServers( ResourceHostMetrics serverMetrics ) throws StrategyException
    {
        // using each strategy criteria, grade servers one by one
        Map<ResourceHostMetric, Integer> grades = new HashMap<>();
        for ( ResourceHostMetric a : serverMetrics.getResources() )
        {
            grades.put( a, 0 );
        }
        for ( Criteria sf : getDistributionCriteria() )
        {
            ResourceHostMetric a = getBestMatch( serverMetrics, MetricComparator.create( sf ) );
            if ( a != null )
            {
                incrementGrade( grades, a );
            }
        }

        // sort servers by their grades in decreasing order
        List<Map.Entry<ResourceHostMetric, Integer>> ls = Lists.newArrayList( grades.entrySet() );
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

        List<ResourceHostMetric> servers = Lists.newArrayList();
        for ( Map.Entry<ResourceHostMetric, Integer> e : ls )
        {
            servers.add( e.getKey() );
        }
        return servers;
    }


    private void incrementGrade( Map<ResourceHostMetric, Integer> grades, ResourceHostMetric a )
    {
        for ( Map.Entry<ResourceHostMetric, Integer> entry : grades.entrySet() )
        {
            if ( entry.getKey().equals( a ) )
            {
                entry.setValue( entry.getValue() + 1 );
                break;
            }
        }
    }


    private ResourceHostMetric getBestMatch( ResourceHostMetrics serverMetrics, final MetricComparator mc )
    {

        List<ResourceHostMetric> ls = Lists.newArrayList( serverMetrics.getResources() );
        Collections.sort( ls, new Comparator<ResourceHostMetric>()
        {

            @Override
            public int compare( ResourceHostMetric o1, ResourceHostMetric o2 )
            {
                double v1 = mc.getValue( o1 );
                double v2 = mc.getValue( o2 );
                return Double.compare( v1, v2 );
            }
        } );

        int ind = mc.isLessBetter() ? 0 : ls.size() - 1;
        return ls.get( ind );
    }


    @Override
    public boolean hasCriteria()
    {
        return true;
    }


    @Override
    public List<CriteriaDef> getCriteriaDef()
    {
        List<CriteriaDef> list = Lists.newArrayList();
        CriteriaDef c = new CriteriaDef<>( "MORE_HDD", "More HDD", false );
        list.add( c );
        c = new CriteriaDef<>( "MORE_RAM", "More RAM", false );
        list.add( c );
        c = new CriteriaDef<>( "MORE_CPU", "More CPU", false );
        list.add( c );

        return Collections.unmodifiableList( list );
    }
}
