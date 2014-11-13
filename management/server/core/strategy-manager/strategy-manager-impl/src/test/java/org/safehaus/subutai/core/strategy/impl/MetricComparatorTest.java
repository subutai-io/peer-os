package org.safehaus.subutai.core.strategy.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MetricComparatorTest {
    MetricComparator metricComparator;
    @Before
    public void setUp() throws Exception {
        metricComparator = new MetricComparator() {
            @Override
            int getValue(ServerMetric m) {
                return 0;
            }
        };
    }

    @Test
    public void testCreate() throws Exception {
    }

    @Test
    public void testGetValueFreeHddMb() throws Exception {
        ServerMetric serverMetric = mock( ServerMetric.class );
        assertEquals(serverMetric.getFreeHddMb(), metricComparator.getValue(serverMetric));
    }

    @Test
    public void testGetValueCpuLoadPercent() throws Exception {
        ServerMetric serverMetric = mock( ServerMetric.class );
        assertEquals(serverMetric.getCpuLoadPercent(),metricComparator.getValue(serverMetric));
    }

    @Test
    public void testGetValueFreeRamMb() throws Exception {
        ServerMetric serverMetric = mock(ServerMetric.class);
        assertEquals(serverMetric.getFreeRamMb(),metricComparator.getValue(serverMetric));
    }
}