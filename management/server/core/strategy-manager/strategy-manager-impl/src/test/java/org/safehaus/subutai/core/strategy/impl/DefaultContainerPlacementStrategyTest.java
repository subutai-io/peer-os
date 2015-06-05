package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.util.UnitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class DefaultContainerPlacementStrategyTest
{
    private static int MB = 1024 * 1024;
    private static int GB = MB * 1024;

    DefaultContainerPlacementStrategy defaultContainerPlacementStrategy;

    @Mock
    ResourceHostMetric metric;


    @Before
    public void setUp() throws Exception
    {
        defaultContainerPlacementStrategy = new DefaultContainerPlacementStrategy();
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( "DEFAULT-STRATEGY", defaultContainerPlacementStrategy.getId() );
    }


    @Test
    public void testGetTitle() throws Exception
    {
        assertEquals( "Default container placement strategy", defaultContainerPlacementStrategy.getTitle() );
    }

    @Test
    public void testCalculateSlotsByRam()
    {
        List<ResourceHostMetric> serverMetrics = new ArrayList<>();

        when( metric.getAvailableRam() ).thenReturn( GB * 8.0 );
        when( metric.getAvailableDiskVar() ).thenReturn( GB * 4096.0 );


        Map<ResourceHostMetric, Integer> result =
                defaultContainerPlacementStrategy.calculateSlots( 1, Arrays.asList( metric ) );

        assertNotNull( result );

        assertEquals( 1, result.size() );

        assertEquals( new Double( ( UnitUtil.getBytesInMb( GB * 8.0 )
                        - DefaultContainerPlacementStrategy.MIN_RAM_IN_RESERVE_MB )
                        / DefaultContainerPlacementStrategy.MIN_RAM_LXC_MB ).intValue(),
                result.entrySet().iterator().next().getValue().intValue() );
    }


    @Test
    public void testCalculateSlotsByHdd()
    {
        List<ResourceHostMetric> serverMetrics = new ArrayList<>();

        when( metric.getAvailableRam() ).thenReturn( GB * 1024.0 );
        when( metric.getAvailableDiskVar() ).thenReturn( GB * 1024.0 );


        Map<ResourceHostMetric, Integer> result =
                defaultContainerPlacementStrategy.calculateSlots( 1, Arrays.asList( metric ) );

        assertNotNull( result );

        assertEquals( 1, result.size() );

        assertEquals( new Double( ( UnitUtil.getBytesInMb( GB * 1024.0 )
                        - DefaultContainerPlacementStrategy.MIN_HDD_IN_RESERVE_MB )
                        / DefaultContainerPlacementStrategy.MIN_HDD_LXC_MB ).intValue(),
                result.entrySet().iterator().next().getValue().intValue() );
    }


    @Test
    public void testCalculatePlacement()
    {
        when( metric.getAvailableRam() ).thenReturn( GB * 1024.0 );
        when( metric.getAvailableDiskVar() ).thenReturn( GB * 1024.0 );

        List<ResourceHostMetric> metrics = new ArrayList<>();
        metrics.add( metric );
        defaultContainerPlacementStrategy.calculatePlacement( 10, metrics, null );
        Map<ResourceHostMetric, Integer> result = defaultContainerPlacementStrategy.getPlacementDistribution();
        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertEquals( 10, result.entrySet().iterator().next().getValue().intValue() );
    }
}