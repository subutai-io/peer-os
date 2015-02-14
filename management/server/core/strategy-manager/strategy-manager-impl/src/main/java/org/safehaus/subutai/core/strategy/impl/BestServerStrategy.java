package org.safehaus.subutai.core.strategy.impl;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.CriteriaDef;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;

import com.google.common.collect.Lists;


public class BestServerStrategy extends RoundRobinStrategy
{
    private List<Criteria> criteria = Lists.newArrayList();


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
    protected List<ServerMetric> sortServers( List<ServerMetric> serverMetrics ) throws StrategyException
    {
        // using each strategy criteria, grade servers one by one
        Map<ServerMetric, Integer> grades = new HashMap<>();
        for ( ServerMetric a : serverMetrics )
        {
            grades.put( a, 0 );
        }
        for ( Criteria sf : criteria )
        {
            ServerMetric a = getBestMatch( serverMetrics, MetricComparator.create( sf ) );
            if ( a != null )
            {
                incrementGrade( grades, a );
            }
        }

        // sort servers by their grades in decreasing order
        List<Map.Entry<ServerMetric, Integer>> ls = Lists.newArrayList( grades.entrySet() );
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

        List<ServerMetric> servers = Lists.newArrayList();
        for ( Map.Entry<ServerMetric, Integer> e : ls )
        {
            servers.add( e.getKey() );
        }
        return servers;
    }


    private void incrementGrade( Map<ServerMetric, Integer> grades, ServerMetric a )
    {
        for ( Map.Entry<ServerMetric, Integer> entry : grades.entrySet() )
        {
            if ( entry.getKey().equals( a ) )
            {
                entry.setValue( entry.getValue() + 1 );
                break;
            }
        }
    }


    private ServerMetric getBestMatch( List<ServerMetric> serverMetrics, final MetricComparator mc )
    {

        List<ServerMetric> ls = Lists.newArrayList( serverMetrics );
        Collections.sort( ls, new Comparator<ServerMetric>()
        {

            @Override
            public int compare( ServerMetric o1, ServerMetric o2 )
            {
                int v1 = mc.getValue( o1 );
                int v2 = mc.getValue( o2 );
                return Integer.compare( v1, v2 );
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
