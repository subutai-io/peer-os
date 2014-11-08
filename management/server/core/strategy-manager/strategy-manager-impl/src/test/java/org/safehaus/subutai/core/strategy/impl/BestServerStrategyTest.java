package org.safehaus.subutai.core.strategy.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import static org.junit.Assert.*;

/**
 * Created by ermek on 10/9/14.
 */
public class BestServerStrategyTest {
    BestServerStrategy bestServerStrategy;
    ServerMetric serverMetric;

    @Before
    public void setUp() throws Exception {
        bestServerStrategy = new BestServerStrategy();
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("BEST_SERVER",bestServerStrategy.getId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals("Best server placement strategy",bestServerStrategy.getTitle());
    }

    @Test
    public void testHasCriteria() throws Exception {
        assertTrue(bestServerStrategy.hasCriteria());
    }

    @Test
    public void testGetCriteria() throws Exception {
        assertNotNull(bestServerStrategy.getCriteria());
    }


    @Test
    public void testSortServers() throws Exception {

    }

}