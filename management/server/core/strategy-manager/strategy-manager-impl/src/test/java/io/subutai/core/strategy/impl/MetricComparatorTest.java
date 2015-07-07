package io.subutai.core.strategy.impl;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.protocol.Criteria;
import io.subutai.core.strategy.api.StrategyException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MetricComparatorTest
{
    private final static double DELTA = 1e-15;
    @Mock
    ResourceHostMetric resourceHostMetric;


    @Test
    public void testHddComparator() throws StrategyException
    {
        when( resourceHostMetric.getAvailableRam() ).thenReturn( 4096.0 );
        when( resourceHostMetric.getAvailableDiskVar() ).thenReturn( 1234567.0 );
        when( resourceHostMetric.getUsedCpu() ).thenReturn( 123.0 );

        Criteria criteria = new Criteria( "MORE_HDD", null );

        MetricComparator c = MetricComparator.create( criteria );
        assertEquals( c.getValue( resourceHostMetric ), 1234567.0, DELTA );
    }


    @Test
    public void testRamComparator() throws StrategyException
    {
        when( resourceHostMetric.getAvailableRam() ).thenReturn( 4096.0 );
        when( resourceHostMetric.getAvailableDiskVar() ).thenReturn( 1234567.0 );
        when( resourceHostMetric.getUsedCpu() ).thenReturn( 123.0 );

        Criteria criteria = new Criteria( "MORE_RAM", null );

        MetricComparator c = MetricComparator.create( criteria );
        assertEquals( c.getValue( resourceHostMetric ), 4096.0, DELTA );
    }


    @Test
    public void testCpuComparator() throws StrategyException
    {
        when( resourceHostMetric.getAvailableRam() ).thenReturn( 4096.0 );
        when( resourceHostMetric.getAvailableDiskVar() ).thenReturn( 1234567.0 );
        when( resourceHostMetric.getUsedCpu() ).thenReturn( 123.0 );

        Criteria criteria = new Criteria( "MORE_CPU", null );

        MetricComparator c = MetricComparator.create( criteria );
        assertEquals( c.getValue( resourceHostMetric ), 123.0, DELTA );
    }
}