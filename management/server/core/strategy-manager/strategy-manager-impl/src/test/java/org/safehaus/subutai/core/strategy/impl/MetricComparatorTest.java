package org.safehaus.subutai.core.strategy.impl;


import org.junit.Before;
import org.safehaus.subutai.common.metric.ResourceHostMetric;


public class MetricComparatorTest
{
    MetricComparator metricComparator;


    @Before
    public void setUp() throws Exception
    {
        metricComparator = new MetricComparator()
        {
            @Override
            double getValue( ResourceHostMetric m )
            {
                return 0;
            }
        };
    }
}