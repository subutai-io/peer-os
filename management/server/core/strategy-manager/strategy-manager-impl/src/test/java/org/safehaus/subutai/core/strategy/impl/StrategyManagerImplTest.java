package org.safehaus.subutai.core.strategy.impl;


import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.StrategyException;

import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;


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
    public void testRegisterUnregisterStrategy()
    {
        strategyManager.registerStrategy( defaultContainerPlacementStrategy );
        List<ContainerPlacementStrategy> strategies = strategyManager.getPlacementStrategies();

        assertNotNull( strategies );
        assertEquals( 1, strategies.size() );

        strategyManager.unregisterStrategy( defaultContainerPlacementStrategy );

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
        Map<Agent, Integer> result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 3, roundRobinPlacementStrategy.getId(), null );
        assertNotNull( result );
        TestCase.assertEquals( 3, result.size() );

        result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 5, roundRobinPlacementStrategy.getId(), null );
        assertNotNull( result );

        result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 15, roundRobinPlacementStrategy.getId(), null );
        assertNotNull( result );

        result = strategyManager
                .getPlacementDistribution( MockUtils.getServerMetrics(), 20, roundRobinPlacementStrategy.getId(), null );
        assertNotNull( result );

        strategyManager.unregisterStrategy( roundRobinPlacementStrategy );
    }
}
