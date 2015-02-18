package org.safehaus.subutai.core.strategy.impl;


import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


/**
 * Created by ermek on 10/9/14.
 */
public class RoundRobinStrategyTest
{
    RoundRobinStrategy roundRobinStrategy;


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
}