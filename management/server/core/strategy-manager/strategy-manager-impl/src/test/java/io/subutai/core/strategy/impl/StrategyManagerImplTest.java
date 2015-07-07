package io.subutai.core.strategy.impl;


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
import org.safehaus.subutai.common.protocol.Criteria;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.StrategyException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class StrategyManagerImplTest
{
    StrategyManagerImpl strategyManager;
    ContainerPlacementStrategy defaultContainerPlacementStrategy;
    ContainerPlacementStrategy roundRobinPlacementStrategy;
    @Mock
    ResourceHostMetric metric1;
    @Mock
    ResourceHostMetric metric2;


    @Before
    public void setUpMethod()
    {
        strategyManager = new StrategyManagerImpl();
        defaultContainerPlacementStrategy = MockUtils.getDefaultContainerPlacementStrategy();
        roundRobinPlacementStrategy = MockUtils.getRoundRobinPlacementStrategy();
    }


    @Test
    public void testRegisterStrategy()
    {
        strategyManager.registerStrategy( defaultContainerPlacementStrategy );
        List<ContainerPlacementStrategy> strategies = strategyManager.getPlacementStrategies();

        assertNotNull( strategies );
        assertEquals( 1, strategies.size() );
    }


    @Test
    public void testUnregisterStrategy()
    {
        strategyManager.unregisterStrategy( defaultContainerPlacementStrategy );
        List<ContainerPlacementStrategy> strategies;

        strategies = strategyManager.getPlacementStrategies();
        assertNotNull( strategies );
        assertEquals( 0, strategies.size() );
    }


    @Test
    public void testGetPlacementStrategies()
    {
        List<ContainerPlacementStrategy> strategies = strategyManager.getPlacementStrategies();
        assertNotNull( strategies );
        assertEquals( 0, strategies.size() );

        strategyManager.registerStrategy( defaultContainerPlacementStrategy );
        strategyManager.registerStrategy( roundRobinPlacementStrategy );

        strategies = strategyManager.getPlacementStrategies();
        assertEquals( 2, strategies.size() );

        assertTrue( strategies.contains( defaultContainerPlacementStrategy ) );
        assertTrue( strategies.contains( roundRobinPlacementStrategy ) );
    }


    @Test
    public void testFindStategyById() throws Exception
    {
        strategyManager.registerStrategy( roundRobinPlacementStrategy );
        assertNotNull( strategyManager.findStrategyById( "ROUND_ROBIN" ) );
        assertEquals( roundRobinPlacementStrategy, strategyManager.findStrategyById( "ROUND_ROBIN" ) );
    }


    @Test
    public void testGetPlacementDistribution() throws StrategyException
    {
        strategyManager.registerStrategy( roundRobinPlacementStrategy );
        List<ContainerPlacementStrategy> strategies = strategyManager.getPlacementStrategies();
        assertNotNull( strategies );
        assertEquals( 1, strategies.size() );
        assertTrue( strategies.contains( roundRobinPlacementStrategy ) );

        when( metric1.getHost() ).thenReturn( "host1" );
        when( metric2.getHost() ).thenReturn( "host2" );
        List<ResourceHostMetric> metrics = Arrays.asList( metric1, metric2 );

        Map<ResourceHostMetric, Integer> distribution =
                strategyManager.getPlacementDistribution( metrics, 3, "ROUND_ROBIN", new ArrayList<Criteria>(  ) );

        assertNotNull( distribution );
        assertEquals( 2, distribution.size() );

        assertEquals( 2, distribution.get( metric1 ).intValue() );
        assertEquals( 1, distribution.get( metric2 ).intValue() );
    }
}
