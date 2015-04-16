package org.safehaus.subutai.core.strategy.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;


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
    }


    @Test
    public void testFindStategyById() throws Exception
    {
        strategyManager.registerStrategy( roundRobinPlacementStrategy );
        ContainerPlacementStrategy containerPlacementStrategy;
        assertNotNull( strategyManager.findStrategyById( "ROUND_ROBIN" ) );
        assertEquals( roundRobinPlacementStrategy, strategyManager.findStrategyById( "ROUND_ROBIN" ) );
    }
}
