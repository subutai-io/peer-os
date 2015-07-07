package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.protocol.Criteria;
import io.subutai.core.strategy.api.StrategyException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RoundRobinStrategyTest
{
    private static final double DELTA = 1e-15;
    private static final String LESS_METRIC_HOST_NAME = "less_server";
    private static final String BEST_METRIC_HOST_NAME = "best_server";
    private static int MB = 1024 * 1024;
    private static int GB = MB * 1024;
    RoundRobinStrategy roundRobinStrategy;
    @Mock
    ResourceHostMetric metric;

    @Mock
    ResourceHostMetric bestMetric;


    @Before
    public void setUp() throws Exception
    {
        roundRobinStrategy = new RoundRobinStrategy();
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( "ROUND_ROBIN", roundRobinStrategy.getId() );
    }


    @Test
    public void testGetTitle() throws Exception
    {
        assertEquals( "Round Robin placement strategy", roundRobinStrategy.getTitle() );
    }


    @Test
    public void testCalculatePlacement() throws StrategyException
    {
        when( metric.getHost() ).thenReturn( LESS_METRIC_HOST_NAME );
        when( metric.getAvailableRam() ).thenReturn( GB * 1024.0 );
        when( metric.getAvailableDiskVar() ).thenReturn( GB * 1024.0 );

        when( bestMetric.getHost() ).thenReturn( LESS_METRIC_HOST_NAME );
        when( bestMetric.getAvailableRam() ).thenReturn( GB * 2048.0 );
        when( bestMetric.getAvailableDiskVar() ).thenReturn( GB * 4096.0 );

        List<ResourceHostMetric> metrics = new ArrayList<>();
        metrics.add( metric );
        metrics.add( bestMetric );
        roundRobinStrategy.calculatePlacement( 3, metrics, new ArrayList<Criteria>() );
        Map<ResourceHostMetric, Integer> result = roundRobinStrategy.getPlacementDistribution();
        assertNotNull( result );
        assertEquals( 2, result.size() );
    }
}