package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.CriteriaDef;
import org.safehaus.subutai.core.strategy.api.StrategyException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class BestServerStrategyTest
{
    private static final double DELTA = 1e-15;
    private static final String LESS_METRIC_HOST_NAME = "less_server";
    private static final String BEST_METRIC_HOST_NAME = "best_server";
    private static int MB = 1024 * 1024;
    private static int GB = MB * 1024;
    @Mock
    ResourceHostMetric metric;

    @Mock
    ResourceHostMetric bestMetric;

    BestServerStrategy bestServerStrategy;


    @Before
    public void setUp() throws Exception
    {
        bestServerStrategy = new BestServerStrategy();
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( "BEST_SERVER", bestServerStrategy.getId() );
    }


    @Test
    public void testGetTitle() throws Exception
    {
        assertEquals( "Best server placement strategy", bestServerStrategy.getTitle() );
    }


    @Test
    public void testHasCriteria() throws Exception
    {
        assertTrue( bestServerStrategy.hasCriteria() );
    }


    @Test
    public void testGetCriteria() throws Exception
    {
        List<CriteriaDef> result = bestServerStrategy.getCriteriaDef();
        assertEquals( 3, result.size() );
        assertNotNull( bestServerStrategy.getCriteriaDef() );
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

        Criteria criteria = new Criteria( "MORE_HDD", true );
        bestServerStrategy.calculatePlacement( 5, metrics, Arrays.asList( criteria ) );
        Map<ResourceHostMetric, Integer> result = bestServerStrategy.getPlacementDistribution();
        assertNotNull( result );
        Assert.assertEquals( 2, result.size() );
    }
}