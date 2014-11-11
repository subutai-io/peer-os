package org.safehaus.subutai.core.strategy.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
        List<Criteria> result = bestServerStrategy.getCriteria();
        assertEquals(3,result.size());
        assertNotNull(bestServerStrategy.getCriteria());
    }


    @Test
    public void testSortServers() throws Exception {
        Map<MetricType, Double> averageMetrics = mock(Map.class);
        ServerMetric serverMetric = new ServerMetric("test", 1, 1, 5, 50, averageMetrics);
        ServerMetric serverMetric1 = new ServerMetric("test1",10,10,50,500,averageMetrics);

        List<ServerMetric> serverMetrics = new ArrayList<>();
        serverMetrics.add(serverMetric);
        serverMetrics.add(serverMetric1);

        assertNotNull(bestServerStrategy.sortServers(serverMetrics));
        List<ServerMetric> result = bestServerStrategy.sortServers(serverMetrics);
        assertEquals(2,result.size());
    }

}