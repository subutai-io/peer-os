package org.safehaus.subutai.core.strategy.impl;


import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by timur on 10/6/14.
 */
public class StrategyManagerImplTest
{
    StrategyManagerImpl strategyManager;
    ContainerPlacementStrategy defaultContainerPlacementStrategy;
    ContainerPlacementStrategy roundRobinPlacementStrategy;


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
    public void testUnregisterStrategy() {
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
    }


    @Test
    public void testRoundRobinPlacementStrategy() throws StrategyException
    {
        strategyManager.registerStrategy( roundRobinPlacementStrategy );
        Map<ServerMetric, Integer> result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 3, roundRobinPlacementStrategy.getId(), null );
        assertNotNull( result );
        TestCase.assertEquals( 3, result.size() );

        result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 5, roundRobinPlacementStrategy.getId(), null );
        assertNotNull( result );

        result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 15, roundRobinPlacementStrategy.getId(),
                        null );
        assertNotNull( result );

        result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 20, roundRobinPlacementStrategy.getId(),
                        null );
        assertNotNull( result );

        strategyManager.unregisterStrategy( roundRobinPlacementStrategy );
    }

    @Test
    public void testFindStategyById() throws Exception {
        strategyManager.registerStrategy( roundRobinPlacementStrategy );
        ContainerPlacementStrategy containerPlacementStrategy;
        assertNotNull(strategyManager.findStrategyById("ROUND_ROBIN"));
        assertEquals(roundRobinPlacementStrategy,strategyManager.findStrategyById("ROUND_ROBIN"));
    }

    @Test
    public void testGetPlacementDistribution() throws Exception {
        strategyManager.registerStrategy( roundRobinPlacementStrategy );
        ServerMetric serverMetric = mock( ServerMetric.class );
        ServerMetric serverMetric1 = mock( ServerMetric.class );
        List<ServerMetric> serverMetrics = new ArrayList(  );
        serverMetrics.add( serverMetric );
        serverMetrics.add( serverMetric1 );
        when(serverMetric.getHostname()).thenReturn( "Server Metric" );
        when(serverMetric1.getHostname()).thenReturn( "Server Metric1" );

        List<Criteria> criterias = mock(ArrayList.class);
        Map<ServerMetric, Integer> result = strategyManager.getPlacementDistribution(serverMetrics,1,"ROUND_ROBIN",criterias);
        assertNotNull(result);

        ContainerPlacementStrategy containerPlacementStrategy = mock(ContainerPlacementStrategy.class);
    }
}
