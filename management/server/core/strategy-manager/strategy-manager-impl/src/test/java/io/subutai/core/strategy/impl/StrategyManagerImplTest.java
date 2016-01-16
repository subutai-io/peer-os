package io.subutai.core.strategy.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class StrategyManagerImplTest
{
    StrategyManagerImpl strategyManager;
    ContainerPlacementStrategy masterPlacementStrategy;
    @Mock
    ResourceHostMetric metric1;
    @Mock
    ResourceHostMetric metric2;


    @Before
    public void setUpMethod()
    {
        strategyManager = new StrategyManagerImpl();
        masterPlacementStrategy = MockUtils.getMasterPlacementStrategy();
//        roundRobinPlacementStrategy = MockUtils.getRoundRobinPlacementStrategy();
    }


    @Test
    public void testRegisterStrategy()
    {
        strategyManager.registerStrategy( masterPlacementStrategy );
        List<ContainerPlacementStrategy> strategies = strategyManager.getPlacementStrategies();

        assertNotNull( strategies );
        assertEquals( 1, strategies.size() );
    }


    @Test
    public void testUnregisterStrategy()
    {
        strategyManager.unregisterStrategy( masterPlacementStrategy );
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

        strategyManager.registerStrategy( masterPlacementStrategy );

        strategies = strategyManager.getPlacementStrategies();
        assertEquals( 1, strategies.size() );

        assertTrue( strategies.contains( masterPlacementStrategy ) );
    }


    @Test
    public void testFindStategyById() throws Exception
    {
        strategyManager.registerStrategy( masterPlacementStrategy );
        assertNotNull( strategyManager.findStrategyById( "MASTER-STRATEGY" ) );
    }


//    @Test
//    public void testGetPlacementDistribution() throws StrategyException
//    {
//        strategyManager.registerStrategy( roundRobinPlacementStrategy );
//        List<ContainerPlacementStrategy> strategies = strategyManager.getPlacementStrategies();
//        assertNotNull( strategies );
//        assertEquals( 1, strategies.size() );
//        assertTrue( strategies.contains( roundRobinPlacementStrategy ) );
//
//        when( metric1.getHostName() ).thenReturn( "host1" );
//        when( metric2.getHostName() ).thenReturn( "host2" );
//        ResourceHostMetrics metrics = new ResourceHostMetrics();
//        metrics.addMetric( metric1 );
//        metrics.addMetric( metric2 );
//
//        Map<ResourceHostMetric, Integer> distribution =
//                strategyManager.getPlacementDistribution( metrics, 3, "ROUND_ROBIN", new ArrayList<Criteria>(  ) );
//
//        assertNotNull( distribution );
//        assertEquals( 2, distribution.size() );
//
//        assertEquals( 2, distribution.get( metric1 ).intValue() );
//        assertEquals( 1, distribution.get( metric2 ).intValue() );
//    }
}
