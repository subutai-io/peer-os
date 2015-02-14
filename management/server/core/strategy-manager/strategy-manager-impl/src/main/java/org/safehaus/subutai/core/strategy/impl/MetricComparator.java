package org.safehaus.subutai.core.strategy.impl;


import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.StrategyException;


abstract class MetricComparator
{

    static MetricComparator create( Criteria criteria ) throws StrategyException
    {
        MetricComparator mc;
        if ( "MORE_HDD".equals( criteria.getId() ) )
        {
            mc = new MetricComparator()
            {
                @Override
                public double getValue( ResourceHostMetric m )
                {
                    return m.getAvailableDiskVar();
                }
            };
        }
        else if ( "MORE_RAM".equals( criteria.getId() ) )
        {
            mc = new MetricComparator()
            {
                @Override
                double getValue( ResourceHostMetric m )
                {
                    return m.getAvailableRam();
                }
            };
        }
        else if ( "MORE_CPU".equals( criteria.getId() ) )
        {
            mc = new MetricComparator()
            {
                @Override
                double getValue( ResourceHostMetric m )
                {
                    return m.getUsedCpu();
                }


                @Override
                boolean isLessBetter()
                {
                    return true;
                }
            };
        }
        else
        {
            throw new StrategyException(
                    String.format( "Comparator not defined for criteria [%s]", criteria.getId() ) );
        }
        return mc;
    }


    abstract double getValue( ResourceHostMetric m );


    boolean isLessBetter()
    {
        return false;
    }
}
