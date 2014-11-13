package org.safehaus.subutai.core.strategy.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by ermek on 10/9/14.
 */
public class RoundRobinStrategyTest {
    RoundRobinStrategy roundRobinStrategy;
    @Before
    public void setUp() throws Exception {
        roundRobinStrategy = new RoundRobinStrategy();
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("ROUND_ROBIN",roundRobinStrategy.getId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals("Round Robin placement strategy",roundRobinStrategy.getTitle());
    }

    @Test
    public void testCalculatePlacement() throws Exception {
        ServerMetric serverMetric = mock( ServerMetric.class );
        ServerMetric serverMetric1 = mock( ServerMetric.class );
        List<ServerMetric> serverMetrics = new ArrayList(  );
        serverMetrics.add( serverMetric );
        serverMetrics.add( serverMetric1 );

        when(serverMetric.getHostname()).thenReturn( "Server Metric" );
        when(serverMetric1.getHostname()).thenReturn( "Server Metric1" );

        roundRobinStrategy.calculatePlacement( 1, serverMetrics, new ArrayList<Criteria>(  ) );

        verify( serverMetric ).getHostname();
    }
}