package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultContainerPlacementStrategyTest {
    DefaultContainerPlacementStrategy defaultContainerPlacementStrategy;

    @Before
    public void setUp() throws Exception {
        defaultContainerPlacementStrategy = new DefaultContainerPlacementStrategy();
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("DEFAULT-STRATEGY", defaultContainerPlacementStrategy.getId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals("Default container placement strategy", defaultContainerPlacementStrategy.getTitle());
    }

    @Test
    public void testCalculatePlacement() throws Exception {
        Map<ServerMetric, Integer> sortedBestServers = mock(Map.class);

        ServerMetric serverMetric = mock(ServerMetric.class);
        ServerMetric serverMetric1 = mock(ServerMetric.class);
        List<ServerMetric> serverMetrics = new ArrayList();
        serverMetrics.add(serverMetric);
        serverMetrics.add(serverMetric1);

        when(serverMetric.getHostname()).thenReturn("Server Metric");
        when(serverMetric1.getHostname()).thenReturn("Server Metric1");

        defaultContainerPlacementStrategy.calculatePlacement(1, serverMetrics, new ArrayList<Criteria>());

    }

    @Test
    public void testCalculateSlots() throws Exception {
        Map<MetricType, Double> averageMetrics = mock(Map.class);
        ServerMetric serverMetric = new ServerMetric("test", 100000, 10000, 500, 5, averageMetrics);
        List<ServerMetric> Metrics = new ArrayList();
        Metrics.add(serverMetric);

        Map<ServerMetric, Integer> serverSlots = new HashMap<>();
        serverSlots.put(serverMetric, 15);

        assertEquals(serverSlots, defaultContainerPlacementStrategy.calculateSlots(1, Metrics));


    }

}