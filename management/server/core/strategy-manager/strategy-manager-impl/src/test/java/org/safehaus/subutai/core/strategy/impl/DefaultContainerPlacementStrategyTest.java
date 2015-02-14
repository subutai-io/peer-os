package org.safehaus.subutai.core.strategy.impl;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DefaultContainerPlacementStrategyTest
{
    DefaultContainerPlacementStrategy defaultContainerPlacementStrategy;


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
}